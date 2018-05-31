package com.susie.oh.model

import com.susie.oh.model.convert.RequestConverterFactory

case class OrderBookRequest(val leg: Leg, val requestFactory: RequestConverterFactory)