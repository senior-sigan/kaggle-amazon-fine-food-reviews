package it.sevenbits.amazonfinefoods

import org.apache.spark.sql.types._
import org.apache.spark.sql.{Dataset, SparkSession}

object App {

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

    val df = spark.read.format("csv").option("header", "true").option("escape", "\"").schema(schema).csv(args(0)).as[Review]

    df.show(10)
    findMostActiveUser(df, spark)
    findMostCommentedFood(df, spark)
    countWords(filterWords(df, spark), spark)
  }

  def findMostActiveUser(df: Dataset[Review], spark: SparkSession): Unit = {
    import spark.implicits._

    df.groupBy("UserId")
      .count()
      .orderBy($"count".desc)
      .show(10)
  }

  def findMostCommentedFood(df: Dataset[Review], spark: SparkSession): Unit = {
    import spark.implicits._

    df.groupBy("ProductId")
      .count()
      .orderBy($"count".desc)
      .show(10)
  }

  def countWords(ds: Dataset[String], spark: SparkSession): Unit = {
    import spark.implicits._

    ds.groupByKey(_.toLowerCase)
      .count()
      .orderBy($"count(1)".desc)
      .show(10)
  }

  def filterWords(ds: Dataset[Review], spark: SparkSession): Dataset[String] = {
    import spark.implicits._

    ds.flatMap(_.Text.split("\\s+"))
      .map(_.trim.replaceAll("[^\\p{Alpha}\\p{Digit}]+", ""))
      .filter(_.nonEmpty)
  }
}
