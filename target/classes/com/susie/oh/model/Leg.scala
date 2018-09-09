package com.susie.oh.model

case class Leg(val sold: String, val bought: String) {
  def swap() = this.copy(sold = this.bought, bought = this.sold)
}