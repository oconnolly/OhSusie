package com.susie.oh.model

import scala.concurrent.duration.FiniteDuration

import com.susie.oh.model.convert.RequestConverterFactory
import com.susie.oh.model.convert.BittrexRequestConverterFactory
import com.susie.oh.model.convert.BinanceRequestConverterFactory
import com.susie.oh.model.convert.PoloniexRequestConverterFactory
import com.susie.oh.model.convert.OkexRequestConverterFactory
import scala.concurrent.duration.Duration
import java.util.concurrent.TimeUnit

case class ExchangeProfile(val id: String, val address: String,
    val fee: Double, val timeout: FiniteDuration, val requestConverterFactory: RequestConverterFactory)

object ExchangeProfile {
  
  val minimumVolume = Map("BTC" -> 0.00001, "ETH" -> 0.0001, "LTC" -> 0.0006, "BNB" -> 0.007, "XRP" -> 0.1, "BCC" -> 0.0001)
  
  def load(): Map[String, ExchangeProfile] = {
    
    Seq(
        ExchangeProfile("BINANCE", "https://www.binance.com/api/v1/depth", 0.0005, Duration(10, TimeUnit.SECONDS), new BinanceRequestConverterFactory()),
        ExchangeProfile("OKEX", "https://www.okex.com/api/v1/depth.do", 0.002, Duration(10, TimeUnit.SECONDS), new OkexRequestConverterFactory()),
        ExchangeProfile("POLONIEX", "https://poloniex.com/public", 0.0025, Duration(10, TimeUnit.SECONDS), new PoloniexRequestConverterFactory()),
        ExchangeProfile("BITTREX", "https://bittrex.com/api/v1.1/public", 0.0025, Duration(10, TimeUnit.SECONDS), new BittrexRequestConverterFactory()))
            .map { profile => profile.id -> profile }.toMap
            
  }
  
}