package com.xebia.openkitchen

import spray.json.DefaultJsonProtocol
import spray.httpx.SprayJsonSupport
import ProductDomain._
import CartMessages._
trait JsonSerializers extends DefaultJsonProtocol with SprayJsonSupport {
  //Product
    implicit val androidFormat = jsonFormat2(Android.apply)
    implicit val batteryFormat = jsonFormat3(Battery.apply)
    implicit val connectivityFormat = jsonFormat5(Connectivity.apply)
    implicit val displayFormat = jsonFormat3(Display.apply)
    implicit val hardwareFormat = jsonFormat6(Hardware.apply)
    implicit val cameraFormat = jsonFormat2(Camera.apply)
    implicit val sAndWFormat = jsonFormat2(SizeAndWeight.apply)
    implicit val storageFormat = jsonFormat2(Storage.apply)
    implicit val deviceFormat = jsonFormat14(Device.apply)
    
    //Messages
    implicit val addToCartFormat = jsonFormat1(AddToCartRequest.apply)
    implicit val removeFromCartFormat = jsonFormat1(RemoveFromCartRequest.apply)
    implicit val cartFormat = jsonFormat2(ShoppingCartItem.apply)
    implicit val orderFormat = jsonFormat2(OrderStateResponse.apply)

}