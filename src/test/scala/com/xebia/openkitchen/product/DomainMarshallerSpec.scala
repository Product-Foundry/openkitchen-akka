package com.xebia.openkitchen
package product

import org.specs2.mutable.Specification
import spray.http._
import spray.http.HttpCharsets._
import spray.http.MediaTypes._
import spray.json._
import spray.json.DefaultJsonProtocol._
import scala.collection.JavaConversions._
import cart.SimpleCartActor._
import api.JsonSerializers
class DomainMarshallerSpec extends Specification with JsonSerializers {
  "Product Repo" should {
    "be initialize correctly" in {
      val repo = ProductStore.apply()
      repo.products.size must be_>(1)
    }
  }
  "Cart items" should {
    "be serializable" in {
      val repo = ProductStore.apply()
      val jsonAst = Seq(ShoppingCartItem(repo.products.head, 1)).toJson
      val cart = jsonAst.convertTo[Seq[ShoppingCartItem]]
      cart.size ==== 1
    }
  }
}