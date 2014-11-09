package com.xebia.openkitchen
import java.io.File
import spray.json.JsonParser
import scala.io.Source
import java.net.URI
import ProductDomain._
import akka.actor._
import spray.json.JsonParser

import scala.io.Source

/**
 * Product repository trait
 */
class ProductRepo(val products: Seq[Device]) {
  lazy val productMap: Map[String, Device] = products.map(p => p.id -> p).toMap
}

object ProductRepo extends JsonSerializers {
  def apply():ProductRepo = {
    val products = productFilePaths.map { path =>
      val productStr = Source.fromInputStream(getClass.getResourceAsStream(path)).mkString
      val jsonAst = JsonParser(productStr)
      jsonAst.convertTo[Device]
    }
    new ProductRepo(products)
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
    import org.json4s.native.JsonMethods._
    val productsStr = Source.fromInputStream(ProductRepoExtension.getClass.getResourceAsStream(s"$productDirRoot/phones.json")).mkString
    val jsonAst = parse(productsStr)
    val productsJStr = jsonAst \\ "id" \\ classOf[JString]
    productsJStr.map(p => s"$productDirRoot/$p.json")
  }

}
/**
 * Extension
 */
private[openkitchen] class ProductRepoExtensionImpl(val productRepo:ProductRepo) extends Extension {
}

private[openkitchen] object ProductRepoExtension extends ExtensionId[ProductRepoExtensionImpl] with ExtensionIdProvider  {//extends ExtensionKey[ProductRepoExtension]
 override def lookup = ProductRepoExtension

  override def createExtension(system: ExtendedActorSystem) =
    new ProductRepoExtensionImpl(ProductRepo())
}

/**
 * Extension trait
 */
trait ProductRepoSupportProvider extends Serializable {
  def system: ActorSystem
  lazy val productRepo = ProductRepoExtension(system).productRepo
}

trait ActorContextProductRepoSupport extends ProductRepoSupportProvider {
  def context: ActorContext
  def system = context.system
}






