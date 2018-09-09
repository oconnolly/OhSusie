package com.susie.oh.init

import com.susie.oh.model.{Leg, OrderBookRequest}

import scala.concurrent.duration.FiniteDuration

trait SusieSettings {
  
  val trades: Seq[(OrderBookRequest, FiniteDuration)]

  val paths: Seq[List[Leg]]
  
}