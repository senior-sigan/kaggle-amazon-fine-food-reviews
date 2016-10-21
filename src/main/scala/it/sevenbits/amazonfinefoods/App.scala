package it.sevenbits.amazonfinefoods

import org.apache.spark.sql.types._
import org.apache.spark.sql.{Dataset, Row, SparkSession}
import org.apache.spark.sql.functions._

object App {

  /**
    * First arguments must be absolute path to the csv file.
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
    
    findMostActiveUser(ds, spark).show(10)
    findMostCommentedFood(ds, spark).show(10)
    findMostPopularWords(ds, spark).show(10)

//    sentences(ds, spark)
  }

  def findMostActiveUser(ds: Dataset[Review], spark: SparkSession): Dataset[Row] = {
    import spark.implicits._

    ds.groupBy("UserId")
      .count()
      .orderBy($"count".desc)
  }

  def findMostCommentedFood(ds: Dataset[Review], spark: SparkSession): Dataset[Row] = {
    import spark.implicits._

    ds.groupBy("ProductId")
      .count()
      .orderBy($"count".desc)
  }

  def findMostPopularWords(ds: Dataset[Review], spark: SparkSession): Dataset[Row] = {
    countWords(filterWords(ds, spark), spark).toDF()
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
      .map(_.trim.replaceAll("[^\\p{Alpha}\\p{Digit}]+", ""))
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
    * @param ds
    * @param n translate API limitation
    * @param spark used for importing implicits
    */
  def sentences(ds: Dataset[Review], n: Int = 1000, spark: SparkSession): Unit = {
    import spark.implicits._

    ds.flatMap { row =>
      if (row.Text.length > n) {
        row.Text.split(Array('.', ',', '?', '!', ';', ':')).map(ch => CommentChunk(row.Id, ch))
      } else {
        Seq(CommentChunk(row.Id, row.Text))
      }
    }.map(ch => ch.copy(Text = ch.Text.trim))
      .filter(_.Text.nonEmpty)
      .show(10)
  }
}
