package com.susie.oh.main

import java.util.concurrent.TimeUnit

import scala.concurrent.duration.Duration

import com.susie.oh.actor.DeciderActor
import com.susie.oh.actor.PriceActor
import com.susie.oh.actor.TradeActor
import com.susie.oh.model.ExchangeProfile
import com.susie.oh.model.Leg
import com.susie.oh.model.OrderBookRequest
import com.susie.oh.model.Triangle
import com.susie.oh.model.convert.BinanceRequestConverterFactory
import com.susie.oh.model.convert.OkexRequestConverterFactory

import akka.actor.ActorSystem
import akka.actor.Props
import akka.stream.ActorMaterializer
import scala.io.Source
import com.susie.oh.model.convert.PoloniexRequestConverterFactory
import akka.routing.RoundRobinPool
import com.susie.oh.model.convert.BittrexRequestConverterFactory
import com.typesafe.config.ConfigFactory
import com.susie.oh.actor.OutboundActor

object Runner {
  
  def main(args: Array[String]): Unit = {
    
    implicit val sys = ActorSystem("ActorSystem", ConfigFactory.load())
    implicit val mat = ActorMaterializer()
    
    val triangles = Seq(
        Triangle(Leg("BTC", "USDT"), Leg("USDT", "ETH"), Leg("ETH", "BTC")),
        Triangle(Leg("BTC", "ETH"), Leg("ETH", "USDT"), Leg("USDT", "BTC")),
        Triangle(Leg("USDT", "ETH"), Leg("ETH", "LTC"), Leg("LTC", "USDT")),
        Triangle(Leg("USDT", "BTC"), Leg("BTC", "ETH"), Leg("ETH", "USDT")),
        Triangle(Leg("USDT", "ETH"), Leg("ETH", "BTC"), Leg("BTC", "USDT")),
        Triangle(Leg("USDT", "BTC"), Leg("BTC", "LTC"), Leg("LTC", "USDT")),
        Triangle(Leg("USDT", "LTC"), Leg("LTC", "BTC"), Leg("BTC", "USDT")),
        Triangle(Leg("ETH", "BTC"), Leg("BTC", "LTC"), Leg("LTC", "ETH")),
        Triangle(Leg("ETH", "LTC"), Leg("LTC", "BTC"), Leg("BTC", "ETH")),
        Triangle(Leg("USDT", "BNB"), Leg("BNB", "ETH"), Leg("ETH", "USDT")),
        Triangle(Leg("USDT", "LTC"), Leg("LTC", "BNB"), Leg("BNB", "USDT")),
        Triangle(Leg("USDT", "BTC"), Leg("BTC", "BNB"), Leg("BNB", "USDT")),
        Triangle(Leg("USDT", "BNB"), Leg("BNB", "BTC"), Leg("BTC", "USDT")),
        Triangle(Leg("USDT", "BNB"), Leg("BNB", "LTC"), Leg("LTC", "USDT")),
        Triangle(Leg("USDT", "ETH"), Leg("ETH", "BNB"), Leg("BNB", "USDT")),
        Triangle(Leg("BTC", "BNB"), Leg("BNB", "LTC"), Leg("LTC", "BTC")),
        Triangle(Leg("LTC", "ETH"), Leg("ETH", "BNB"), Leg("BNB", "LTC")),
        Triangle(Leg("BTC", "LTC"), Leg("LTC", "BNB"), Leg("BNB", "BTC")),
        Triangle(Leg("LTC", "BNB"), Leg("BNB", "ETH"), Leg("ETH", "LTC")),
        Triangle(Leg("BTC", "ETH"), Leg("ETH", "BNB"), Leg("BNB", "BTC")),
        Triangle(Leg("BTC", "BNB"), Leg("BNB", "ETH"), Leg("ETH", "BTC")))
    
    val priceActorRouter = sys.actorOf(RoundRobinPool(3).props(Props(new PriceActor(mat))).withDispatcher("akka.my-dispatcher"))
    
    val tradeActorRouter = sys.actorOf(RoundRobinPool(2).props(Props(new TradeActor(mat))))
        
    val deciderActor = sys.actorOf(Props(new DeciderActor(triangles, tradeActorRouter)), name = "DeciderActor")
    
    val outboundActor = sys.actorOf(Props[OutboundActor], name = "OutboundActor")
    
    val binanceProfile = ExchangeProfile("BINANCE", "https://www.binance.com/api/v1/depth", 0.0005, Duration(10, TimeUnit.SECONDS), new BinanceRequestConverterFactory())
    
    val okexProfile = ExchangeProfile("OKEX", "https://www.okex.com/api/v1/depth.do", 0.002, Duration(10, TimeUnit.SECONDS), new OkexRequestConverterFactory())
    
    val poloniexProfile = ExchangeProfile("POLONIEX", "https://poloniex.com/public", 0.0025, Duration(10, TimeUnit.SECONDS), new PoloniexRequestConverterFactory())
    
    val bittrexProfile = ExchangeProfile("BITTREX", "https://bittrex.com/api/v1.1/public", 0.0025, Duration(10, TimeUnit.SECONDS), new BittrexRequestConverterFactory())
    
    sys.scheduler.schedule(Duration.Zero, Duration(1, TimeUnit.SECONDS)) {
      
      priceActorRouter ! OrderBookRequest("BTC", "ETH", binanceProfile)
      priceActorRouter ! OrderBookRequest("BTC", "ETH", okexProfile)
      priceActorRouter ! OrderBookRequest("BTC", "ETH", bittrexProfile)
    
      priceActorRouter ! OrderBookRequest("USDT", "BTC", binanceProfile)
      priceActorRouter ! OrderBookRequest("USDT", "BTC", okexProfile)
      priceActorRouter ! OrderBookRequest("USDT", "BTC", bittrexProfile)
    
      priceActorRouter ! OrderBookRequest("USDT", "ETH", binanceProfile)
      priceActorRouter ! OrderBookRequest("USDT", "ETH", okexProfile)
      priceActorRouter ! OrderBookRequest("USDT", "ETH", bittrexProfile)
      
      priceActorRouter ! OrderBookRequest("BTC", "BNB", binanceProfile)
      priceActorRouter ! OrderBookRequest("ETH", "BNB", binanceProfile)
      priceActorRouter ! OrderBookRequest("USDT", "BNB", binanceProfile)
      priceActorRouter ! OrderBookRequest("BNB", "LTC", binanceProfile)
      
      priceActorRouter ! OrderBookRequest("ETH", "LTC", binanceProfile)
      priceActorRouter ! OrderBookRequest("BTC", "LTC", binanceProfile)
      priceActorRouter ! OrderBookRequest("USDT", "LTC", binanceProfile)
      
      priceActorRouter ! OrderBookRequest("ETH", "LTC", okexProfile)
      priceActorRouter ! OrderBookRequest("BTC", "LTC", okexProfile)
      priceActorRouter ! OrderBookRequest("USDT", "LTC", okexProfile)
      
      priceActorRouter ! OrderBookRequest("ETH", "LTC", bittrexProfile)
      priceActorRouter ! OrderBookRequest("BTC", "LTC", bittrexProfile)
      priceActorRouter ! OrderBookRequest("USDT", "LTC", bittrexProfile)
      
    }(sys.dispatcher)
    
    /*sys.scheduler.schedule(Duration.Zero, Duration(2, TimeUnit.SECONDS)) {
      priceActorRouter ! OrderBookRequest("BTC", "ETH", poloniexProfile)
      priceActorRouter ! OrderBookRequest("USDT", "BTC", poloniexProfile)
      priceActorRouter ! OrderBookRequest("USDT", "ETH", poloniexProfile)
      priceActorRouter ! OrderBookRequest("BTC", "LTC", poloniexProfile)
      priceActorRouter ! OrderBookRequest("USDT", "LTC", poloniexProfile)
    }(sys.dispatcher)*/
    
    while(true) {
      
      Thread.sleep(1000L)
      
    }
    
    sys.terminate()
    
  }
  
}
