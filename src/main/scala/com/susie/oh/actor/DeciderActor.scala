package com.susie.oh.actor

import akka.actor.Actor
import akka.actor.ActorLogging
import com.susie.oh.model.Price
import com.susie.oh.model.Triangle

class DeciderActor(val triangles: Seq[Triangle]) extends Actor with ActorLogging {
  
  val data = new collection.mutable.HashMap[(String, String, String), (Double, Double)]()
  
  override def receive = {
    
    case p @ Price(sold, bought, exchangeId, price) => {
      
      data += ((sold, bought, exchangeId) -> (price, Double.NaN))
      
      doSomething(p)
      
    }
    
    case anythingElse => log.warning(s"Unrecognized message: $anythingElse")
    
  }
  
  private def doSomething(price: Price) {
    
    System.err.println(data)
    
    val pair = (price.sold, price.bought)
    
    triangles.map { tri =>
      
      System.err.print("tri: " + tri + " pair: " + pair)
      
      if(tri.first == pair || tri.second == pair || tri.third == pair) {
        
        val first = (tri.first._1, tri.first._2, "OKEX")
        
        val firstPrice = data.get(first).map(_._1).getOrElse(-1d)
        
        val second = (tri.second._1, tri.second._2, "OKEX")
        
        val secondPrice = data.get(second).map(_._1).getOrElse(-1d)
        
        val third = (tri.third._1, tri.third._2, "OKEX")
        
        val thirdPrice = data.get(third).map(_._1).getOrElse(-1d)
        
        if(!(firstPrice == -1d || secondPrice == -1d || thirdPrice == -1d)) {
          
          val result = firstPrice * secondPrice * thirdPrice
          
          System.err.println("result is: " + result)
          
          if(result > 1 + 0.01) {
            System.err.println("send trade!")
          }
          
        } else {
          null
        }
        
      }
      
    }
    
  }
  
}