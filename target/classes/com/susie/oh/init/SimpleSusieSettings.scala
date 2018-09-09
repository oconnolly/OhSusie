package com.susie.oh.init

import java.util.concurrent.TimeUnit

import scala.concurrent.duration.FiniteDuration
import scala.io.Source

import com.susie.oh.model.ExchangeProfile
import com.susie.oh.model.Leg
import com.susie.oh.model.OrderBookRequest
import com.susie.oh.model.Triangle
import com.susie.oh.model.convert.RequestConverterFactory

class SimpleSusieSettings(val tradesFilename: String, val trianglesFilename: String, val exchanges: Map[String, RequestConverterFactory]) extends SusieSettings {
  
  private val comma = ","
  
  lazy val tradeFileLines = Source.fromFile(tradesFilename).getLines()
  
  val trades: Seq[(OrderBookRequest, FiniteDuration)] = {
    tradeFileLines.next() // skip header
    tradeFileLines.map { line =>
      val row = line.split(comma)
      val exchange = exchanges(row(2))
      val pollFrequency = FiniteDuration(row(3).toLong, TimeUnit.MILLISECONDS)
      val enabled = row(4) == "Y"
      if(enabled) (OrderBookRequest(Leg(row(0), row(1)), exchange), pollFrequency) else null
    }
    .toSeq
    .filter(_ != null)
  }
  
  val triangles: Seq[Triangle] = Triangle.load()

  val paths = PathsGenerator.run().toSeq
  
}