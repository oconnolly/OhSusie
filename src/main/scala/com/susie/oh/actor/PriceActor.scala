package com.susie.oh.actor

import com.susie.oh.model.ExchangeProfile
import com.susie.oh.model.OrderBookRequest
import akka.stream.ActorMaterializer
import scala.concurrent.Await
import com.susie.oh.model.Leg
import scala.util.Success
import scala.util.Failure
import org.apache.commons.lang.exception.ExceptionUtils

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
    
    orderBookRequest.exchangeProfile.requestConverterFactory.getResponse(orderBookRequest, httpResponse).onComplete {
      case Success((firstPrice, secondPrice)) => {
        deciderActor ! firstPrice
        deciderActor ! secondPrice
      }
      case Failure(e) => log.error(ExceptionUtils.getFullStackTrace(e))
    }
    
  }
  
}