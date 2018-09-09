package com.susie.oh.actor

import akka.stream.ActorMaterializer
import com.susie.oh.model.Price

class TradeActor(override val mat: ActorMaterializer) extends BaseExchangeActor(mat) {
  
  override def receive = {
    
    case TradeRequest(ratio, prices) => {
      
      /* val price2VolumeConverted = price2.price * price2.volume
      
      val price3VolumeConverted = price3.volume * (1 / price1.price)
      
      val lowestVolume = Seq(price1.volume, price2VolumeConverted, price3VolumeConverted).min
      
      val price2VolumeOriginalUnit = lowestVolume * (1 / price2.price)
      
      val price3VolumeOriginalUnit = lowestVolume * price1.price */
      
      // log.info(s"!!! Ratio: $ratio Execute trades with volumes: $price1 $lowestVolume; $price2 $price2VolumeOriginalUnit; $price3 $price3VolumeOriginalUnit !!!")

      val maxPriceAge = System.currentTimeMillis() - prices.map(_.timestamp).min

      log.info("!!! Ratio is: " + ratio + ". # legs: " + prices.size + ". Staleness (ms): " + maxPriceAge + ". Execute: " + prices)

      // TODO add logic here to avoid duplicate trades
      
    }
    
  }
  
}

case class TradeRequest(val ratio: Double, val prices: Seq[Price])