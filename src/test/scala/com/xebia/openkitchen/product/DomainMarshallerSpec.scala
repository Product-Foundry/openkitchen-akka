package com.xebia.openkitchen
package product

import org.specs2.mutable.Specification
import spray.http._
import spray.http.HttpCharsets._
import spray.http.MediaTypes._
import spray.json._
import spray.json.DefaultJsonProtocol._
import scala.collection.JavaConversions._
import cart.CartDomain._
import api.JsonSerializers
import org.scalatest.junit.JUnitRunner
import org.scalatest.Matchers
import org.scalatest.WordSpecLike
import org.junit.runner.RunWith
@RunWith(classOf[JUnitRunner])
class DomainMarshallerSpec extends WordSpecLike with Matchers with JsonSerializers {
  "Product Repo" should {
    "be initialize correctly" in {
      val repo = ProductStore.apply()
      repo.products.size should be > 1
    }
  }
  "Cart items" should {
    "be serializable" in {
      val repo = ProductStore.apply()
      val jsonAst = Seq(CartItem(repo.products.head, 1)).toJson
      val cart = jsonAst.convertTo[Seq[CartItem]]
      cart.size should be(1)
    }
  }
}