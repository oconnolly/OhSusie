package com.susie.oh.init

import com.susie.oh.actor.DeciderActor
import com.susie.oh.actor.OutboundActor
import com.susie.oh.actor.PriceActor
import com.susie.oh.actor.TradeActor
import com.susie.oh.model.ExchangeProfile
import com.typesafe.config.ConfigFactory

import akka.actor.ActorSystem
import akka.actor.Props
import akka.routing.RoundRobinPool
import akka.stream.ActorMaterializer

object Susie {
  
  val config = ConfigFactory.load()
  
  val susieSettings: SusieSettings = new SimpleSusieSettings("data/tradelist.csv", null, ExchangeProfile.load())
  
  implicit val sys = ActorSystem("ActorSystem", config)
  implicit val mat = ActorMaterializer()
  
  val numberOfPriceActors = config.getInt("susie.numberOfPriceActors")
  
  val numberOfTradeActors = config.getInt("susie.numberOfTradeActors")
  
  val priceActorRouter = sys.actorOf(RoundRobinPool(numberOfPriceActors).props(Props(new PriceActor(mat))), "PriceActor")
  
  val tradeActorRouter = sys.actorOf(RoundRobinPool(numberOfTradeActors).props(Props(new TradeActor(mat))), "TradeActor")
        
  val deciderActor = sys.actorOf(Props(new DeciderActor(susieSettings.triangles, tradeActorRouter)), "DeciderActor")
    
  val outboundActor = sys.actorOf(Props[OutboundActor], "OutboundActor")
  
}