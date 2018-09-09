package com.susie.oh.init

import scala.concurrent.duration.FiniteDuration
import com.susie.oh.model.{Leg, OrderBookRequest, Triangle}

trait SusieSettings {
  
  val trades: Seq[(OrderBookRequest, FiniteDuration)]
  
  val triangles: Seq[Triangle]

  val paths: Seq[List[Leg]]
  
}