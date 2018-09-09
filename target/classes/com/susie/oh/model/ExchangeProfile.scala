package com.susie.oh.model

import scala.concurrent.duration.FiniteDuration
import com.susie.oh.model.convert._

import scala.concurrent.duration.Duration
import java.util.concurrent.TimeUnit

import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec
import java.util.Base64


case class ExchangeProfile(val id: String, val address: String,
    val tradeAddress: String, val marketFee: Double, val limitFee: Double, val timeout: FiniteDuration)

object ExchangeProfile {
  
  val minimumVolume = Map("BTC" -> 0.00001, "ETH" -> 0.0001, "LTC" -> 0.0006, "BNB" -> 0.007, "XRP" -> 0.1, "BCC" -> 0.0001)
  
  private val dur = Duration(10, TimeUnit.SECONDS)
  
  def load(): Map[String, RequestConverterFactory] = factories
  
  private lazy val factories = Seq(
        new BinanceRequestConverterFactory2(ExchangeProfile("BINANCE", "https://www.binance.com/api/v1/depth", null, 0.0005, 0.0005, dur)),
        new OkexRequestConverterFactory2(ExchangeProfile("OKEX", "https://www.okex.com/api/v1/depth.do", null, 0.002, 0.0015, dur)),
        new PoloniexRequestConverterFactory2(ExchangeProfile("POLONIEX", "https://poloniex.com/public", null, 0.002, 0.001, dur)),
        new BittrexRequestConverterFactory2(ExchangeProfile("BITTREX", "https://bittrex.com/api/v1.1/public", null, 0.0025, 0.0025, dur)),
        new KuCoinRequestConverterFactory2(ExchangeProfile("KUCOIN", "https://api.kucoin.com/v1/open/orders", null, 0.001, 0.001, dur)))
            .map { factory => factory.exchangeProfile.id -> factory }.toMap
  
  @Deprecated // TODO move somewhere else
  def getHash(queryString: String, secretKey: String): String = {
    
    val mac = Mac.getInstance("HmacSHA256")
    
    val secretKeySpec = new SecretKeySpec(secretKey.getBytes(), "HmacSHA256")
    
    mac.init(secretKeySpec)
    
    val bytes = mac.doFinal(queryString.getBytes())
    
    val hash = new StringBuilder()
    
    (0 until bytes.length).foreach { i =>
      val hex = Integer.toHexString(0xFF & bytes(i))
      if(hex.length() == 1) hash.append('0')
      hash.append(hex)
    }
    
    return hash.toString()
    
  }
  
  def main(args: Array[String]): Unit = {
    // val str = "symbol=LTCBTC&side=BUY&type=LIMIT&timeInForce=GTC&quantity=1&price=0.1&recvWindow=5000&timestamp=1499827319559"
    // val str = s"symbol=LTCBTC&side=BUY&type=LIMIT&timeInForce=GTC&quantity=1&price=0.1&recvWindow=30000&timestamp=${System.currentTimeMillis()}"
    
    // System.err.println(getHash(str, "IsjRLPqRsL9jZhA64hAka9YpR2VTP5qPSQlMvklIbN6zlkdBFUvrV3IstxV39djg"))
    
    val requestFactory = ExchangeProfile.load()("BINANCE")
    
    new BinanceRequestConverterFactory2(requestFactory.exchangeProfile)
    
  }
  
}