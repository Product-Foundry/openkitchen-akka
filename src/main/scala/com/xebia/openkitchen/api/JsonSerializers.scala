package com.xebia.openkitchen
package api

import spray.json.DefaultJsonProtocol
import spray.httpx.SprayJsonSupport
import product.ProductJsonSerializers
import Api._
import cart.CartDomain._

trait JsonSerializers extends ProductJsonSerializers with SprayJsonSupport {
    implicit val addToCartFormat = jsonFormat1(AddToCartRequest.apply)
    implicit val removeFromCartFormat = jsonFormat1(RemoveFromCartRequest.apply)
    implicit val cartFormat = jsonFormat2(CartItem.apply)
    implicit val orderFormat = jsonFormat2(OrderStateResponse.apply)

}