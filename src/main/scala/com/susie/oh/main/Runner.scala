package com.susie.oh.main

import akka.http.scaladsl.Http
import akka.http.scaladsl.model.HttpRequest
import akka.actor.ActorSystem
import scala.concurrent.Await
import scala.concurrent.duration.Duration
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.stream.ActorMaterializer
import scala.concurrent.duration.FiniteDuration
import java.util.concurrent.TimeUnit
import scala.concurrent.Future

object Runner extends JsonSupport {
  
  val BUFFER = 0.01
  
  def main(args: Array[String]) {
    
    implicit val sys = ActorSystem()
    implicit val mat = ActorMaterializer()
    
    import sys.dispatcher
    
    sys.scheduler.schedule(Duration.Zero, FiniteDuration(3, TimeUnit.SECONDS))(doThing)
    
    while(true) {
      Thread.sleep(3000L)
    }
    
    // sys.terminate()
    
  }
  
  def doThing()(implicit sys: ActorSystem, mat: ActorMaterializer) {
    
    val val1 = Await.result({ Future {
      1 / getBidAsks("https://www.okex.com/api/v1/depth.do?symbol=ltc_btc").bids(0)(0)
    }(sys.dispatcher) }, Duration("3 seconds"))
    
    val val2 = Await.result({ Future {
      getBidAsks("https://www.okex.com/api/v1/depth.do?symbol=eth_btc").asks.last(0)
    }(sys.dispatcher) }, Duration("3 seconds"))
    
    val val3 = Await.result({ Future {
      getBidAsks("https://www.okex.com/api/v1/depth.do?symbol=ltc_eth").asks.last(0)
    }(sys.dispatcher) }, Duration("3 seconds"))
    
    val result = val1 * val2 * val3
    System.err.println(s"val1: $val1 val2: $val2 val3: $val3 result: $result")
    
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