package com.susie.oh.actor

import akka.actor.Actor
import akka.actor.ActorLogging
import akka.http.scaladsl.Http
import akka.stream.ActorMaterializer

abstract class BaseExchangeActor(val mat: ActorMaterializer) extends Actor with ActorLogging {
  
  implicit val ec = context.system.dispatcher
  implicit val materializer = mat
  
  val http = Http(context.system)
  
}