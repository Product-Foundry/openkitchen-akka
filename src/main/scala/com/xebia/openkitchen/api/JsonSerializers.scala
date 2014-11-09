package com.xebia.openkitchen
package api

import spray.json.DefaultJsonProtocol
import spray.httpx.SprayJsonSupport
import product.ProductJsonSerializers
import cart.SimpleCartActor._
import Api._
trait JsonSerializers extends ProductJsonSerializers with SprayJsonSupport {
    //Messages
    implicit val addToCartFormat = jsonFormat1(AddToCartRequest.apply)
    implicit val removeFromCartFormat = jsonFormat1(RemoveFromCartRequest.apply)
    implicit val cartFormat = jsonFormat2(ShoppingCartItem.apply)
    implicit val orderFormat = jsonFormat2(OrderStateResponse.apply)

}