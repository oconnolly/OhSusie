package com.susie.oh.main

import java.util.concurrent.TimeUnit

import scala.concurrent.Await
import scala.concurrent.Future
import scala.concurrent.duration.Duration
import scala.concurrent.duration.FiniteDuration

import com.susie.oh.actor.DeciderActor
import com.susie.oh.actor.PriceActor
import com.susie.oh.actor.TradeActor
import com.susie.oh.model.ExchangeProfile

import akka.actor.ActorSystem
import akka.actor.Props
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.HttpRequest
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.stream.ActorMaterializer
import com.susie.oh.model.convert.BinanceRequestConverterFactory
import com.susie.oh.model.OrderBookRequest
import com.susie.oh.model.Triangle
import com.susie.oh.model.convert.OkexRequestConverterFactory

object Runner2 {
  
  def main(args: Array[String]): Unit = {
    
    implicit val sys = ActorSystem()
    implicit val mat = ActorMaterializer()
    
    val triangles = Seq(
        Triangle(("BTC", "USDT"), ("USDT", "ETH"), ("ETH", "BTC")),
        Triangle(("BTC", "ETH"), ("ETH", "USDT"), ("USDT", "BTC")))
    
    val deciderActor = sys.actorOf(Props(new DeciderActor(triangles)), name = "DeciderActor")
    
    val exchangeProfile = ExchangeProfile.createNull()
    
    val binanceProfile = ExchangeProfile("BINANCE", "https://www.binance.com/api/v1/depth", Duration(3, TimeUnit.SECONDS), new BinanceRequestConverterFactory())
    
    val okexProfile = ExchangeProfile("OKEX", "https://www.okex.com/api/v1/depth.do", Duration(3, TimeUnit.SECONDS), new OkexRequestConverterFactory())
    
    val priceActors = (1 to 3).map { _ =>
      sys.actorOf(Props(new PriceActor(mat)))
    }
    
    val tradeActors = (1 to 2).map { _ =>
      sys.actorOf(Props(new TradeActor(mat)))
    }
    
    
    while(true) {
      
      priceActors(0) ! OrderBookRequest("BTC", "ETH", okexProfile)
    
      priceActors(1) ! OrderBookRequest("USDT", "BTC", okexProfile)
    
      priceActors(2) ! OrderBookRequest("USDT", "ETH", okexProfile)
      
      Thread.sleep(1000L)
      
    }
    
    sys.terminate()
    
  }
  
}

object Runner extends JsonSupport {
  
  val BUFFER = 0.01
  
  def main(args: Array[String]) {
    
    implicit val sys = ActorSystem()
    implicit val mat = ActorMaterializer()
    
    import sys.dispatcher
    
    sys.scheduler.schedule(Duration.Zero, FiniteDuration(10, TimeUnit.SECONDS))(doThing)
    
    while(true) {
      Thread.sleep(3000L)
    }
    
    // sys.terminate()
    
  }
  
  def doThing()(implicit sys: ActorSystem, mat: ActorMaterializer) {
    
    import sys.dispatcher
    
    val val1 = Future {
      1 / getBidAsks("https://www.okex.com/api/v1/depth.do?symbol=ltc_btc").bids(0)(0)
    }
    
    val val2 = Future {
      getBidAsks("https://www.okex.com/api/v1/depth.do?symbol=eth_btc").asks.last(0)
    }
    
    val val3 = Future {
      getBidAsks("https://www.okex.com/api/v1/depth.do?symbol=ltc_eth").asks.last(0)
    }
    
    val vals = Await.result(Future.sequence(Seq(val1, val2, val3)), Duration("5 seconds"))
    val result = vals.reduce(_ * _)
    System.err.println(s"val1: ${vals(0)} val2: ${vals(1)} val3: ${vals(2)} result: $result")
    
    if(result > 1 + BUFFER) {
      System.err.println("Send trades now!")
    }
  }
  
  def getBidAsks(addr: String)(implicit sys: ActorSystem, mat: ActorMaterializer): BidAskResponse = {
    
    System.err.println(s"Submitting request using URI: $addr")
    
    val futHttpResp = Http().singleRequest(HttpRequest(uri = addr))
    
    val httpResp = Await.result(futHttpResp, Duration("3 seconds"))
    
    val bidAskResponseFut = Unmarshal(httpResp.entity).to[BidAskResponse]
    
    val bidAskResponse = Await.result(bidAskResponseFut, Duration("1 second"))
    
    System.err.println("ASKS: " + bidAskResponse.asks.map(a => a(0) + " " + a(1)).mkString(", "))
    System.err.println("BIDS: " + bidAskResponse.bids.map(a => a(0) + " " + a(1)).mkString(", "))
    
    return bidAskResponse
  }
  
}