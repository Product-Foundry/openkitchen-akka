package com.xebia.openkitchen
package product
import java.io._
import spray.json.JsonParser
import scala.io.Source
import java.net.URI
import scala.io.Source
import api.JsonSerializers
import spray.json.JsonReader

/**
 * Product repository trait
 */
class ProductStore(val products: Seq[Device]) {
  lazy val productMap: Map[String, Device] = products.map(p => p.id -> p).toMap
}

object ProductStore extends ProductJsonSerializers {
  def apply(): ProductStore = {
    val products = productFilePaths.map { path =>
      parse[Device](getClass.getResourceAsStream(path))
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
    val metadata = parse[Seq[DeviceMetaData]](ProductStoreExtension.getClass.getResourceAsStream(s"$productDirRoot/phones.json"))
    metadata.map(p => s"$productDirRoot/${p.id}.json")
  }

  private def parse[T: JsonReader](is: InputStream) = {
    val productsStr = Source.fromInputStream(is).mkString
    val jsonAst = JsonParser(productsStr)
    jsonAst.convertTo[T]
  }

}






