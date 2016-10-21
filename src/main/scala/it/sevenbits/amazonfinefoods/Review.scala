package it.sevenbits.amazonfinefoods

case class Review(
                   Id: Int,
                   ProductId: String,
                   UserId: String,
                   ProfileName: String,
                   HelpfulnessNumerator: Int,
                   HelpfulnessDenominator: Int,
                   Score: Int,
                   Time: Long,
                   Summary: String,
                   Text: String
                 )