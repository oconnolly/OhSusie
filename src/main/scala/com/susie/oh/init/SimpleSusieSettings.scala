package com.susie.oh.init

import java.util.concurrent.TimeUnit

import com.susie.oh.model.convert.RequestConverterFactory
import com.susie.oh.model.{Leg, OrderBookRequest}

import scala.concurrent.duration.FiniteDuration
import scala.io.Source

class SimpleSusieSettings(val tradesFilename: String, val exchanges: Map[String, RequestConverterFactory]) extends SusieSettings {
  
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

  val paths = PathsGenerator.run().toSeq
  
}