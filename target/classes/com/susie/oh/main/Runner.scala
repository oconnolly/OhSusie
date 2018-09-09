package com.susie.oh.main

import scala.concurrent.duration.Duration

object Runner {
  
  def main(args: Array[String]): Unit = {
    
    import com.susie.oh.init.Susie._
    
    susieSettings.trades.groupBy(_._2).foreach { case (duration, requests) =>
      sys.scheduler.schedule(Duration.Zero, duration) {
        requests.foreach { case (request, _) => priceActorRouter ! request }
      }(sys.dispatcher)
    }
    
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
