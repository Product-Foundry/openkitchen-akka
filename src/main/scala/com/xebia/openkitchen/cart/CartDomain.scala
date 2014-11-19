package com.xebia.openkitchen
package cart

import com.xebia.openkitchen.product.Device

object CartDomain {

  case class AddToCartRequest(itemId: String)
  case class RemoveFromCartRequest(itemId: String)
  case object GetCartRequest
  case class CartItem(item: Device, count: Int = 1)

  case object OrderRequest
  sealed trait OrderState
  case class OrderProcessed(orderId: String) extends OrderState
  case object OrderProcessingFailed extends OrderState

  case class CartItems(items: Seq[CartItem] = Seq()) {
    
    def update(item: Device) = {
      val updatedItem = items.find(_.item.id == item.id)
        .map(item => item.copy(count = (item.count + 1)))
        .getOrElse(CartItem(item))
      copy(items = (items.filterNot(_.item.id == item.id) :+ updatedItem))
    }
    
    def remove(item: Device) = {
      copy(items = items.filterNot(_.item.id == item.id))
    }

    def clear() = copy(items = Seq())
    def size = items.size
    def isEmpty = items.isEmpty
    override def toString = s"CartItems: ${items.map(_.item.name).mkString(", ")}"
  }

}