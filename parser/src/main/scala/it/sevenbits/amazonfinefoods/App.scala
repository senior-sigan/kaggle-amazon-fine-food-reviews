package it.sevenbits.amazonfinefoods

import com.rabbitmq.client.{Channel, ConnectionFactory, DefaultConsumer}
import org.apache.spark.sql.types._
import org.apache.spark.sql.{Dataset, Row, SparkSession}

object App {

  /**
    * First arguments must be absolute path to the csv file.
    *
    * @param args
    */
  def main(args: Array[String]): Unit = {
    val spark = SparkSession.builder().appName("amazon-fine-foods").getOrCreate()
    import spark.implicits._

    val schema = StructType(Seq(
      StructField("Id", IntegerType),
      StructField("ProductId", StringType),
      StructField("UserId", StringType),
      StructField("ProfileName", StringType),
      StructField("HelpfulnessNumerator", IntegerType),
      StructField("HelpfulnessDenominator", IntegerType),
      StructField("Score", IntegerType),
      StructField("Time", LongType),
      StructField("Summary", StringType),
      StructField("Text", StringType)
    ))

    val ds = spark.read
      .format("csv")
      .option("header", "true")
      .option("escape", "\"")
      .schema(schema)
      .csv(args(0))
      .as[Review]

    ds.show(10)

    if (args(1) == "translate=true") {
      sentences(ds, 800, spark)
    } else {
      findMostActiveUser(ds, spark).show(100)
      findMostCommentedFood(ds, spark).show(100)
      findMostPopularWords(ds, spark).show(100)
    }
  }

  def findMostActiveUser(ds: Dataset[Review], spark: SparkSession, limit: Int = 1000): Dataset[Row] = {
    import spark.implicits._

    ds.groupBy("UserId", "ProfileName")
      .count()
      .orderBy($"count".desc)
      .limit(limit)
      .orderBy($"ProfileName")
  }

  def findMostCommentedFood(ds: Dataset[Review], spark: SparkSession, limit: Int = 1000): Dataset[Row] = {
    import spark.implicits._

    ds.groupBy("ProductId")
      .count()
      .orderBy($"count".desc)
      .limit(limit)
      .orderBy($"ProductId")
  }

  def findMostPopularWords(ds: Dataset[Review], spark: SparkSession, limit: Int = 1000): Dataset[Row] = {
    import spark.implicits._

    countWords(filterWords(ds, spark), spark)
      .toDF()
      .limit(limit)
      .orderBy($"value")
  }

  def countWords(ds: Dataset[String], spark: SparkSession): Dataset[(String, Long)] = {
    import spark.implicits._

    ds.groupByKey(_.toLowerCase)
      .count()
      .orderBy($"count(1)".desc)
  }

  def filterWords(ds: Dataset[Review], spark: SparkSession): Dataset[String] = {
    import spark.implicits._

    // in addition we can filter out pronouns, prepositions etc...
    ds.flatMap(_.Text.split("\\s+"))
      .map(_.trim.replaceAll("[^\\p{Alpha}]+", ""))
      .filter(_.nonEmpty)
  }

  /**
    * Translation task.
    *
    * Get all text reviews.
    * If the text is bigger than N symbols, where N is translate API limitation, so then try to split the text on sentences.
    * We shouldn't split text somewhere randomly, because it will cause missing some language constructions, so we try to split it by punctuation marks.
    * After splitting we need to join full sentence, so we store review id.
    * So we get huge dataset of short sentences and still long ones.
    * Todo: handle still huge sentences, split randomly, because there is no way to do it better.
    *
    * One interesting thing.
    * We pay for a call to the API. So we must utilize maximum allowed size of the request - N symbols.
    * As we see from dataset there are a lot of really small (40 symbols)  sentences. We can join them in a one big chunk.
    * I suppose this schema: `#review.Id{{review.Text}}`, for example: `#1{{Tasty and sweet cookies}}#2{{Some other review}}...`
    * Translator keep our special characters, so we can use them to reconstruct original sentences. Also characters are quite rare, so we shouldn't face any problem.
    *
    * It's impossible to send every request directly to the translate API, so we need queue of tasks for translation.
    * We can use Kafka, RabbitMq, etc. to store this tasks.
    * Task may throw exception and we must put it in the end of the queue to execute again later.
    *
    * Heuristic params.
    * If n == 800 We get 285000 texts to translate with average text length of 900 symbols.
    * So we utilize translate api by maximum.
    *
    * @param ds
    * @param n     translate API limitation
    * @param spark used for importing implicits
    */
  def sentences(ds: Dataset[Review], n: Int = 1000, spark: SparkSession): Unit = {
    import spark.implicits._

    val accum = new StringAccumulator()
    spark.sparkContext.register(accum, "StringAccumulator")

    ds.flatMap { row =>
      if (row.Text.length > n) {
        row.Text.split(Array('.', ',', '?', '!', ';', ':')).map(ch => CommentChunk(row.Id, ch))
      } else {
        Seq(CommentChunk(row.Id, row.Text))
      }
    }.map(ch => ch.copy(Text = ch.Text.trim))
      .filter(_.Text.nonEmpty)
      .foreachPartition { partition =>
        val queueName = "translate_queue"
        val factory = new ConnectionFactory()
        factory.setHost("172.17.0.2")
        val connection = factory.newConnection()
        val channel = connection.createChannel()
        channel.queueDeclare(queueName, false, false, false, null)

        partition.foreach { chunk =>
          accum.add(s"${chunk.Id}|||${chunk.Text}\n") // actually we could use a pool of accumulators to achieve the best coverage - the biggest avg and smallest total count of requests to the translate api.
          if (accum.value.length >= n) {
            channel.basicPublish("", queueName, null, accum.value.getBytes())
            accum.reset()
          }
        }

        channel.close()
        connection.close()
      }
  }
}
