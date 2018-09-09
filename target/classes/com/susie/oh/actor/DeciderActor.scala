package com.susie.oh.actor

import com.susie.oh.model.Leg
import com.susie.oh.model.Price
import com.susie.oh.model.Triangle
import com.susie.oh.outbound.message.OutboundDataMessage

import akka.actor.Actor
import akka.actor.ActorLogging
import akka.actor.ActorRef

class DeciderActor(val paths: Seq[List[Leg]], val traderService: ActorRef) extends Actor with ActorLogging {
  
  val outboundActor = context.actorSelection("/user/OutboundActor")
  
  import scala.collection.mutable
  
  val data = new mutable.HashMap[(Leg, String), (Double, Double, Long)]()
  
  val lowestPriceData = new mutable.HashMap[Leg, (Double, String)]()
  
  override def receive = {
    
    case p @ Price(leg @ Leg(sold, bought), exchangeId, price, volume, _, timestamp) => {
      
      data += ((leg, exchangeId) -> (price, volume, timestamp))
      
      lowestPriceData get leg match {
        
        case None => {
          
          lowestPriceData.+=((leg, (price, exchangeId)))
          
          doSomething2(p)
          
        }

        // TODO revisit this
        case Some((pr, ex)) if ex == exchangeId || price < pr => {

          lowestPriceData.+=((leg, (price, exchangeId)))
          
          doSomething2(p)
          
        }
        
        case _ => {}
        
      }
      
    }
    
    case anythingElse => log.warning(s"Unrecognized message: $anythingElse")
    
  }
  
  private def doSomething2(newPrice: Price) {
    
    paths.filter { case list =>

      list.contains(newPrice.leg)

    }.foreach { case list =>

      val priceData = list.map { leg =>

        lowestPriceData.get(leg).map { case (_, exch) => (exch, data((leg, exch))) }.getOrElse((null, (-1D, -1D, 0L)))

      }

      val allValidPrices = priceData.map(_._2._1 != -1).fold(true)(_ && _)

      if(allValidPrices) {

        val result = priceData.map(_._2._1).reduce(_ * _)

        // TODO fix this!!
        // outboundActor ! OutboundDataMessage(result, (first, firstPrice._1), (second, secondPrice._1), (third, thirdPrice._1))

        val threshold = 0.995 // - (0.0025 * list.size)

        // System.err.println("result is: " + result)

        if(result <= threshold) {

          val prices = priceData.zipWithIndex.map { case (data, i) =>
            Price(list(i), data._1, data._2._1, data._2._2, timestamp = data._2._3)
          }

          val minTimestamp = prices.map(_.timestamp).min
          val maxTimestamp = prices.map(_.timestamp).max

          // if(maxTimestamp - minTimestamp < 200L) {
            traderService ! TradeRequest(result, prices)
          // }
        }

      }
      
    }
    
  }
  
}
