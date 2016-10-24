package it.sevenbits.amazonfinefoods

import org.apache.spark.util.AccumulatorV2

class StringAccumulator extends AccumulatorV2[String, String] {
  private var value_ = ""

  override def isZero: Boolean = value.isEmpty

  override def copy(): AccumulatorV2[String, String] = {
    val newAcc = new StringAccumulator()
    newAcc.value_ = value_
    newAcc
  }

  override def reset(): Unit = {
    value_ = ""
  }

  override def add(v: String): Unit = {
    value_ += v
  }

  override def merge(other: AccumulatorV2[String, String]): Unit = {
    value_ += other.value
  }

  override def value: String = value_
}
