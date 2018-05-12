package com.susie.oh.actor

import akka.stream.ActorMaterializer

class TradeActor(override val mat: ActorMaterializer) extends BaseExchangeActor(mat) {
  
  override def receive = {
    
    case _ => {} // http.singleRequest(HttpRequest(uri = exchangeProfile.address))
    
  }
  
}