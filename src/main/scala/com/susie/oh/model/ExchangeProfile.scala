package com.susie.oh.model

import com.susie.oh.model.convert.RequestConverterFactory
import scala.concurrent.duration.FiniteDuration

case class ExchangeProfile(val id: String, val address: String,
    val fee: Double, val timeout: FiniteDuration, val requestConverterFactory: RequestConverterFactory)

object ExchangeProfile {
  
  def createNull(): ExchangeProfile = ExchangeProfile(null, null, Double.NaN, null, null)
  
  val rateMap = Map("BTC" -> 0.00001, "ETH" -> 0.0001, "LTC" -> 0.0006, "BNB" -> 0.007)
  
}