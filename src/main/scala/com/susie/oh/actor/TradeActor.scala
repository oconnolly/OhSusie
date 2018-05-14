package com.susie.oh.actor

import akka.stream.ActorMaterializer
import com.susie.oh.model.Price

class TradeActor(override val mat: ActorMaterializer) extends BaseExchangeActor(mat) {
  
  override def receive = {
    
    case TradeRequest(price1, price2, price3) => {
      
      val price2VolumeConverted = price2.price * price2.volume
      
      val price3VolumeConverted = price3.volume * (1 / price1.price)
      
      val lowestVolume = Seq(price1.volume, price2VolumeConverted, price3VolumeConverted).min
      
      val price2VolumeOriginalUnit = lowestVolume * (1 / price2.price)
      
      val price3VolumeOriginalUnit = lowestVolume * price1.price
      
      System.err.println(s"!!! Execute trades with volumes: $price1 $lowestVolume; $price2 $price2VolumeOriginalUnit; $price3 $price3VolumeOriginalUnit !!!")
      
    }
    
  }
  
}

case class TradeRequest(val price1: Price, val price2: Price, val price3: Price)