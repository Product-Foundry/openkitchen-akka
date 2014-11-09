package com.xebia.openkitchen
package product
import akka.actor._
/**
 * Extension
 */
private[openkitchen] class ProductStoreExtensionImpl(val productRepo:ProductStore) extends Extension {
}

private[openkitchen] object ProductStoreExtension extends ExtensionId[ProductStoreExtensionImpl] with ExtensionIdProvider  {//extends ExtensionKey[ProductRepoExtension]
 override def lookup = ProductStoreExtension

  override def createExtension(system: ExtendedActorSystem) =
    new ProductStoreExtensionImpl(ProductStore())
}

/**
 * Extension trait
 */
trait ProductStoreSupportProvider extends Serializable {
  def system: ActorSystem
  lazy val productRepo = ProductStoreExtension(system).productRepo
}

trait ActorContextProductRepoSupport extends ProductStoreSupportProvider {
  def context: ActorContext
  def system = context.system
}
