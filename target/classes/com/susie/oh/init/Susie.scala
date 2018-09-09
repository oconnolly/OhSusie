package com.susie.oh.init

import akka.actor.{ActorSystem, Props}
import akka.routing.RoundRobinPool
import akka.stream.ActorMaterializer
import com.susie.oh.actor.{DeciderActor, OutboundActor, PriceActor, TradeActor}
import com.susie.oh.model.ExchangeProfile
import com.typesafe.config.ConfigFactory

object Susie {
  
  val config = ConfigFactory.load()
  
  val susieSettings: SusieSettings = new SimpleSusieSettings("data/tradelist.csv", null, ExchangeProfile.load())
  
  implicit val sys = ActorSystem("ActorSystem", config)
  implicit val mat = ActorMaterializer()
  
  val numberOfPriceActors = config.getInt("susie.numberOfPriceActors")
  
  val numberOfTradeActors = config.getInt("susie.numberOfTradeActors")
  
  val priceActorRouter = sys.actorOf(RoundRobinPool(numberOfPriceActors).props(Props(new PriceActor(mat))), "PriceActor")
  
  val tradeActorRouter = sys.actorOf(RoundRobinPool(numberOfTradeActors).props(Props(new TradeActor(mat))), "TradeActor")
        
  val deciderActor = sys.actorOf(Props(new DeciderActor(susieSettings.paths, tradeActorRouter)), "DeciderActor")
    
  val outboundActor = sys.actorOf(Props[OutboundActor], "OutboundActor")
  
}