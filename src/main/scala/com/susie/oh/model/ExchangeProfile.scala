package com.susie.oh.model

import scala.concurrent.duration.FiniteDuration

import com.susie.oh.model.convert.RequestConverterFactory

case class ExchangeProfile(val id: String, val address: String,
    val fee: Double, val timeout: FiniteDuration, val requestConverterFactory: RequestConverterFactory)

object ExchangeProfile {
  
  def createNull(): ExchangeProfile = ExchangeProfile(null, null, Double.NaN, null, null)
  
  val minimumVolume = Map("BTC" -> 0.00001, "ETH" -> 0.0001, "LTC" -> 0.0006, "BNB" -> 0.007, "XRP" -> 0.1, "BCC" -> 0.0001)
  
}