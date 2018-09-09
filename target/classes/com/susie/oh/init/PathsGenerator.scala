package com.susie.oh.init

import com.susie.oh.model.Leg

import scala.collection.mutable.ArrayBuffer

object PathsGenerator {

  case class Node(val currency: String, val children: Set[String])

  val BTC = Node("BTC", Set("ETH", "BNB", "LTC", "BCC", "XRP", "KCS", "NEO", "USDT"))

  val ETH = Node("ETH", Set("BTC", "BNB", "LTC", "BCC", "XRP", "NEO", "KCS", "USDT"))

  val BNB = Node("BNB", Set("BTC", "ETH", "LTC", "BCC", "USDT"))

  val LTC = Node("LTC", Set("BTC", "BNB", "ETH", "KCS", "USDT"))

  val USDT = Node("USDT", Set("BTC", "ETH", "BNB", "LTC", "BCC", "XRP", "NEO", "KCS"))

  val XRP = Node("XRP", Set("BTC", "ETH", "BNB", "USDT"))

  val BCC = Node("BCC", Set("BTC", "ETH", "BNB", "USDT"))

  val KCS = Node("KCS", Set("BTC", "ETH", "USDT", "LTC", "NEO"))

  val NEO = Node("NEO", Set("BTC", "ETH", "USDT", "KCS"))

  val nodes = Seq(BTC, ETH, BNB, LTC, USDT, XRP, BCC, KCS, NEO)

  val nodesMap = nodes.map { n => n.currency -> n }.toMap

  def run(): ArrayBuffer[List[Leg]] = {

    val accum = new ArrayBuffer[List[String]]()

    nodes.foreach { node => find(node.currency, List(), node, accum) }

    val accumDistinct = deduplicate(accum)

    accumDistinct.map { convertToLegs }.filter { list => list.size < 7 }

  }

  def convertToLegs(path: List[String]): List[Leg] = {

    val size = path.size
    val res = for(i <- (1 to size)) yield {
      if(i == size) {
        Leg(path(i - 1), path.head)
      } else {
        Leg(path(i - 1), path(i))
      }
    }
    res.toList
  }

  def deduplicate(accum: ArrayBuffer[List[String]]): ArrayBuffer[List[String]] = {

    val set = new collection.mutable.HashSet[Set[String]]()

    accum.filter { case list => set.add(list.toSet) }

  }

  def find(goal: String, path: List[String], node: Node, accum: ArrayBuffer[List[String]]): Unit = {

    node.children.foreach { n =>

      if(!path.contains(n)) {

        if(n == goal) {

          accum += (goal :: path)

        } else {

          find(goal, path ++ Seq(n), nodesMap(n), accum)

        }

      }

    }

  }

}
