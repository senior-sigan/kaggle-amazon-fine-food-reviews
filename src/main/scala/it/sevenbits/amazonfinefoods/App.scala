package it.sevenbits.amazonfinefoods

import org.apache.spark.sql.types._
import org.apache.spark.sql.{DataFrame, SparkSession}

object App {

  def main(args: Array[String]): Unit = {
    val spark = SparkSession.builder().appName("amazon-fine-foods").getOrCreate()

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

    val df = spark.read
      .format("csv")
      .option("header", "true")
      .schema(schema)
      .csv("/home/ilya/Documents/amazon-fine-foods/data/Reviews.csv")

    df.show(10)
    //    findMostActiveUser(df, spark)
    //    findMostCommentedFood(df, spark)
    findMostPopularWord(df, spark)
  }

  def findMostActiveUser(df: DataFrame, spark: SparkSession): Unit = {
    import spark.implicits._

    df.groupBy("UserId")
      .count()
      .orderBy($"count".desc)
      .show(10)
  }

  def findMostCommentedFood(df: DataFrame, spark: SparkSession): Unit = {
    import spark.implicits._

    df.groupBy("ProductId")
      .count()
      .orderBy($"count".desc)
      .show(10)
  }

  def findMostPopularWord(df: DataFrame, spark: SparkSession): Unit = {
    import spark.implicits._

    df.flatMap { row =>
      row.getAs[String]("Text").split(" ")
    }.map { text =>
      text.toLowerCase().trim.replace(".", "").replace(",", "").replace("\"", "")
    }.filter { text =>
      text.nonEmpty
    }.show(100)
  }
}
