package com.susie.oh.actor

import akka.actor.{Actor, ActorLogging, ActorRef}
import com.susie.oh.model.{Leg, Price, PricePair}
import com.susie.oh.outbound.message.OutboundDataMessage

class DeciderActor(val paths: Seq[List[Leg]], val traderService: ActorRef) extends Actor with ActorLogging {
  
  val outboundActor = context.actorSelection("/user/OutboundActor")
  
  import scala.collection.mutable
  
  val data = new mutable.HashMap[(Leg, String), (Double, Double, Long)]()
  
  val lowestPriceData = new mutable.HashMap[Leg, (Double, String)]()
  
  override def receive = {

    case PricePair(p1, p2) => {

      val anyRelevantUpdates = Seq(p1, p2).map(savePrice).reduce(_ && _)

      if(anyRelevantUpdates) calculateTradePaths(p1, p2)

    }
    
    case anythingElse => log.warning(s"Unrecognized message: $anythingElse")
    
  }

  private def savePrice(p: Price): Boolean = {

    data += ((p.leg, p.exchangeId) -> (p.price, p.volume, p.timestamp))

    lowestPriceData get p.leg match {

      case None => {

        lowestPriceData.+=((p.leg, (p.price, p.exchangeId)))

        true

      }

      // TODO revisit this
      case Some((pr, ex)) if ex == p.exchangeId || p.price < pr => {

        lowestPriceData.+=((p.leg, (p.price, p.exchangeId)))

        true

      }

      case _ => false

    }
  }
  
  private def calculateTradePaths(p1: Price, p2: Price) {
    
    paths.filter { case list =>

      list.filter(leg => leg == p1.leg || leg == p2.leg).nonEmpty

    }.foreach { case list =>

      val priceData = list.map { leg =>

        lowestPriceData.get(leg).map { case (_, exch) => (exch, data((leg, exch))) }.getOrElse((null, (-1D, -1D, 0L)))

      }

      val allValidPrices = priceData.map(_._2._1 != -1).reduce(_ && _)

      if(allValidPrices) {

        val result = priceData.map(_._2._1).reduce(_ * _)

        val tradeList = priceData.zip(list).map { case ((exch, _), leg) => (leg, exch) }

        outboundActor ! OutboundDataMessage(result, tradeList)

        if(result <= 0.995) {

          val prices = priceData.zipWithIndex.map { case (data, i) =>
            Price(list(i), data._1, data._2._1, data._2._2, timestamp = data._2._3)
          }

          val minTimestamp = prices.map(_.timestamp).min
          val maxTimestamp = prices.map(_.timestamp).max

          if(maxTimestamp - minTimestamp < 250L) {

            traderService ! TradeRequest(result, prices)

            prices.foreach { p =>
              data.remove((p.leg, p.exchangeId))
              lowestPriceData.remove(p.leg)
            }

          }
        }

      }
      
    }
    
  }
  
}
