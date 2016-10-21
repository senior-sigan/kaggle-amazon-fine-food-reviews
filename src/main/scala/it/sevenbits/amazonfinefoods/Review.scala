package it.sevenbits.amazonfinefoods

case class Review(
                   Id: Int,
                   ProductId: String,
                   UserId: String,
                   ProfileName: String,
                   HelpfulnessNumerator: String,
                   HelpfulnessDenominator: String,
                   Score: String,
                   Time: String,
                   Summary: String,
                   Text: String
                 )