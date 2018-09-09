package com.susie.oh.actor

import akka.stream.ActorMaterializer
import com.susie.oh.model.{OrderBookRequest, PricePair}
import com.susie.oh.outbound.message.OutboundExchangeLatencyMessage

import scala.concurrent.Await
import scala.util.{Failure, Success}

class PriceActor(override val mat: ActorMaterializer) extends BaseExchangeActor(mat) {
  
  val deciderActor = context.actorSelection("/user/DeciderActor")
  
  val outboundActor = context.actorSelection("/user/OutboundActor")
  
  override def preRestart(reason: Throwable, message: Option[Any]) {
    message.foreach { msg =>
      log.error(s"Failed on message: $msg")
    }
  }
  
  override def receive = {
    
    case r: OrderBookRequest => {
      
      val response = getPrices(r)
      
    }
    
  }
  
  def getPrices(orderBookRequest: OrderBookRequest) = {
    
    val httpRequest = orderBookRequest.requestFactory.getRequest(orderBookRequest)
    
    val httpResponse = Await.result(http.singleRequest(httpRequest), orderBookRequest.requestFactory.exchangeProfile.timeout)
    
    val t0 = System.currentTimeMillis()
    
    orderBookRequest.requestFactory.getResponse(orderBookRequest, httpResponse).onComplete {
      case Success((firstPrice, secondPrice)) => {
        val now = System.currentTimeMillis()
        deciderActor ! PricePair(firstPrice.copy(timestamp = now), secondPrice.copy(timestamp = now))
        outboundActor ! OutboundExchangeLatencyMessage(orderBookRequest, System.currentTimeMillis() - t0)
      }
      case Failure(e) => log.error(e.getMessage())
    }
    
  }
  
}