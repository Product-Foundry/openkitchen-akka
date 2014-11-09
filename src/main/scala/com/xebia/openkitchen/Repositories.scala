package com.xebia.openkitchen
import java.io.File
import spray.json.JsonParser
import scala.io.Source
import java.net.URI
import ProductDomain._

/**
 * Product repository trait
 */
trait ProductRepo extends Serializable {
  val products: Seq[Device]
  lazy val productMap: Map[String, Device] = products.map(p => p.id -> p).toMap
}

class ProductRepoImpl(val products: Seq[Device]) extends ProductRepo 

/**
 * Loads products stored in static resources
 * /webapp/root/phones
 * /root/\* will be accessible from the classpath
 */
object ProductRepo extends JsonSerializers{

  def apply(): ProductRepo = {
    val products = productFilePaths.map { path =>
      val productStr = Source.fromInputStream(getClass.getResourceAsStream(path)).mkString
      val jsonAst = JsonParser(productStr)
      jsonAst.convertTo[Device]
    }
    new ProductRepoImpl(products)
  }

  /**
   * Creates a list of paths pointing to each product.json
   * E.g.: /root/phones/dell-streak-7.json, /root/phones/dell-venue.json etc.
   *
   * Necessary to load products this way in order to make it work
   * in test reading from classpath and reading inside jar
   */
  private def productFilePaths: Seq[String] = {
    val productDirRoot = "/root/phones"
    import org.json4s._
    import org.json4s.JsonDSL._
    import org.json4s.native.JsonMethods._
    val productsStr = Source.fromInputStream(ProductRepo.getClass.getResourceAsStream(s"$productDirRoot/phones.json")).mkString
    val jsonAst = parse(productsStr)
    val productsJStr = (jsonAst \\ "id" \\ classOf[JString])
    productsJStr.map(p => s"$productDirRoot/$p.json")
  }

}



/**
 * Session repository trait
 */
trait SessionRepo {
import SimpleCartActor._

  import collection.mutable._

  val sessionState = Map[String, Seq[ShoppingCartItem]]()

  def removeFromCart(sessionId: String, item: Device): Seq[ShoppingCartItem] = {
    val updatedItems = sessionState.get(sessionId)
      .map(_.filterNot(_.item.id == item.id))
      .getOrElse(Seq())
    sessionState += (sessionId -> updatedItems)
    updatedItems
  }

  def getCartItems(sessionId: String) = sessionState.get(sessionId).getOrElse(Seq())

  def checkoutCart(sessionId: String): Seq[ShoppingCartItem] = {
    val items = getCartItems(sessionId)
    sessionState += (sessionId -> Seq())
    items
  }

  def upsertCart(sessionId: String, item: Device): Seq[ShoppingCartItem] = {
    val updatedItems = sessionState.get(sessionId) match {
      case Some(items) => {
        val updatedItem = items.find(_.item.id == item.id)
          .map(item => item.copy(count = (item.count + 1)))
          .getOrElse(ShoppingCartItem(item))
        updatedItem +: items.filterNot(_.item.id == item.id)
      }
      case None => Seq(ShoppingCartItem(item))
    }
    sessionState += (sessionId -> updatedItems)
    updatedItems

  }
}
object SessionRepo extends SessionRepo

