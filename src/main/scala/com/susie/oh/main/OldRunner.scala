package com.susie.oh.main

import scala.concurrent.Await
import akka.stream.ActorMaterializer
import akka.actor.ActorSystem
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.http.scaladsl.Http
import scala.concurrent.duration.Duration
import akka.http.scaladsl.model.HttpRequest
import scala.concurrent.Future
import scala.concurrent.duration.FiniteDuration
import java.util.concurrent.TimeUnit

@Deprecated
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