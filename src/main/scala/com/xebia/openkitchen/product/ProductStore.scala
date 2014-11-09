package com.xebia.openkitchen
package product
import java.io.File
import spray.json.JsonParser
import scala.io.Source
import java.net.URI
import scala.io.Source
import com.xebia.openkitchen.api.JsonSerializers

/**
 * Product repository trait
 */
class ProductStore(val products: Seq[Device]) {
  lazy val productMap: Map[String, Device] = products.map(p => p.id -> p).toMap
}

object ProductStore extends ProductJsonSerializers {
  def apply():ProductStore = {
    val products = productFilePaths.map { path =>
      val productStr = Source.fromInputStream(getClass.getResourceAsStream(path)).mkString
      val jsonAst = JsonParser(productStr)
      jsonAst.convertTo[Device]
    }
    new ProductStore(products)
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
    val productsStr = Source.fromInputStream(ProductStoreExtension.getClass.getResourceAsStream(s"$productDirRoot/phones.json")).mkString
    val jsonAst = parse(productsStr)
    val productsJStr = jsonAst \\ "id" \\ classOf[JString]
    productsJStr.map(p => s"$productDirRoot/$p.json")
  }

}






