package com.susie.oh.model

case class Price(val leg: Leg, val exchangeId: String, val price: Double, val volume: Double, val isSwapped: Boolean = false, val timestamp: Long = 0)

case class PricePair(val price: Price, val inversePrice: Price)