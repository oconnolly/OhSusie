package com.susie.oh.actor

import akka.actor.Actor
import akka.actor.ActorLogging
import com.susie.oh.model.Price
import com.susie.oh.model.Triangle
import com.susie.oh.model.Leg
import scala.collection.mutable.ListBuffer
import com.susie.oh.outbound.OutboundDataMessage

class DeciderActor(val triangles: Seq[Triangle]) extends Actor with ActorLogging {
  
  val outboundActor = context.actorSelection("/user/OutboundActor")
  
  import collection.mutable
  
  val data = new mutable.HashMap[(Leg, String), (Double, Double)]()
  
  val lowestPriceData = new mutable.HashMap[Leg, (Double, String)]()
  
  override def receive = {
    
    case p @ Price(leg @ Leg(sold, bought), exchangeId, price) => {
      
      data += ((leg, exchangeId) -> (price, Double.NaN))
      
      lowestPriceData get leg match {
        
        case None => lowestPriceData.+=((leg, (price, exchangeId)))
        
        case Some((pr, ex)) if ex == exchangeId || price < pr => lowestPriceData.+=((leg, (price, exchangeId)))
        
        case _ => {}
        
      }
      
      doSomething2(p)
      
    }
    
    case anythingElse => log.warning(s"Unrecognized message: $anythingElse")
    
  }
  
  private def doSomething2(newPrice: Price) {
    
    triangles.filter { case Triangle(first, second, third) =>
      
      newPrice.leg == first || newPrice.leg == second || newPrice.leg == third
      
    }.map { case t @ Triangle(first, second, third) =>
      
      val firstPrice = lowestPriceData.get(first).map { case (_, exch) => (exch, data((first, exch))._1) }.getOrElse((null, -1D))
      
      val secondPrice = lowestPriceData.get(second).map { case (_, exch) => (exch, data((second, exch))._1) }.getOrElse(null, -1D)
      
      val thirdPrice = lowestPriceData.get(third).map { case (_, exch) => (exch, data((third, exch))._1) }.getOrElse(null, -1D)
      
      if(firstPrice._2 != -1 && secondPrice._2 != -1 && thirdPrice._2 != -1) {
        
        val result = firstPrice._2 * secondPrice._2 * thirdPrice._2
        
        outboundActor ! OutboundDataMessage(result, (first, firstPrice._1), (second, secondPrice._1), (third, thirdPrice._1))
        
        if(result < 1) {
          
          System.err.println(">>>>>>>>>>>>>>>>>>>>>>>>>")
          
          System.err.println(" result is: " + result)
          
          System.err.println("Triangle: " + t)
        
          System.err.println(s"Prices: p1: $firstPrice p2: $secondPrice p3: $thirdPrice")
          
          System.err.println(" send trade!")
          
          System.err.println("\n-_-_-")
          System.err.println(newPrice)
          System.err.println("first price: " + firstPrice + " for " + first)
          
          data.filter(_._1._1 == first).foreach(System.err.println)
          
          System.err.println("second price: " + secondPrice + " for " + second)
          
          data.filter(_._1._1 == second).foreach(System.err.println)
          
          System.err.println("third price: " + thirdPrice + " for " + third)
          
          data.filter(_._1._1 == third).foreach(System.err.println)
          
          System.err.println("-_-_-\n")
          
          System.err.println("<<<<<<<<<<<<<<<<<<<<<<<<<")
        }
        
      }
      
    }
    
  }
  
}
