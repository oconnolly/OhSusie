package com.susie.oh.actor

import com.susie.oh.model.ExchangeProfile
import com.susie.oh.model.OrderBookRequest
import akka.stream.ActorMaterializer
import scala.concurrent.Await

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
    
    val response = Await.result(orderBookRequest.exchangeProfile.requestConverterFactory.getResponse(orderBookRequest.exchangeProfile, httpResponse), orderBookRequest.exchangeProfile.timeout)
    
    val firstPrice = response._1.copy(sold = orderBookRequest.sold, bought = orderBookRequest.bought)
    
    val secondPrice = response._2.copy(sold = orderBookRequest.bought, bought = orderBookRequest.sold)
    
    deciderActor ! firstPrice
    
    deciderActor ! secondPrice
    
  }
  
}