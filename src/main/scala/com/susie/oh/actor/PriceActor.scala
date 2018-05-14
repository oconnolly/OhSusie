package com.susie.oh.actor

import scala.concurrent.Await
import scala.util.Failure
import scala.util.Success

import org.apache.commons.lang.exception.ExceptionUtils

import com.susie.oh.model.OrderBookRequest

import akka.stream.ActorMaterializer

class PriceActor(override val mat: ActorMaterializer) extends BaseExchangeActor(mat) {
  
  val deciderActor = context.actorSelection("/user/DeciderActor")
  
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
        log.info(s"Finished API request for $orderBookRequest in ${System.currentTimeMillis() - t0} milliseconds")
      }
      case Failure(e) => log.error(ExceptionUtils.getFullStackTrace(e))
    }
    
  }
  
}