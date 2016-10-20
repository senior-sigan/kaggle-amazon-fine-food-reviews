package it.sevenbits.amazonfinefoods

//Id,ProductId,UserId,ProfileName,HelpfulnessNumerator,HelpfulnessDenominator,Score,Time,Summary,Text
case class Review(
                   id: Long,
                   productId: String,
                   userId: String,
                   profileName: String,
                   helpfulnessNumerator: Int,
                   helpfulnessDenominator: Int,
                   score: Int,
                   time: Long,
                   summary: String,
                   text: String
                 )