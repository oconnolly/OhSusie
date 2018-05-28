package com.susie.oh.init

import scala.concurrent.duration.FiniteDuration

import com.susie.oh.model.OrderBookRequest
import com.susie.oh.model.Triangle

trait SusieSettings {
  
  val trades: Seq[(OrderBookRequest, FiniteDuration)]
  
  val triangles: Seq[Triangle]
  
}