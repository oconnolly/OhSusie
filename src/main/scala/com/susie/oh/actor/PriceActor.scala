package com.susie.oh.actor

import scala.concurrent.Await
import scala.util.Failure
import scala.util.Success

import com.susie.oh.model.OrderBookRequest
import com.susie.oh.outbound.message.OutboundExchangeLatencyMessage

import akka.stream.ActorMaterializer

class PriceActor(override val mat: ActorMaterializer) extends BaseExchangeActor(mat) {
  
  val deciderActor = context.actorSelection("/user/DeciderActor")
  
  val outboundActor = context.actorSelection("/user/OutboundActor")
  
  override def receive = {
    
    case r: OrderBookRequest => {
      
      val response = getPrices(r)
      
    }
    
  }
  
  def getPrices(orderBookRequest: OrderBookRequest) = {
    
    val httpRequest = orderBookRequest.exchangeProfile.requestConverterFactory.getRequest(orderBookRequest.exchangeProfile, orderBookRequest)
    
    val httpResponse = Await.result(http.singleRequest(httpRequest), orderBookRequest.exchangeProfile.timeout)
    
    val t0 = System.currentTimeMillis()
    
    orderBookRequest.exchangeProfile.requestConverterFactory.getResponse(orderBookRequest, httpResponse).onComplete {
      case Success((firstPrice, secondPrice)) => {
        deciderActor ! firstPrice
        deciderActor ! secondPrice
        outboundActor ! OutboundExchangeLatencyMessage(orderBookRequest, System.currentTimeMillis() - t0)
      }
      case Failure(e) => log.error(e.getMessage())
    }
    
  }
  
}