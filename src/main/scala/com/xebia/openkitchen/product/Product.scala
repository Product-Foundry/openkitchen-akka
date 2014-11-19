package com.xebia.openkitchen
package product

import java.util.UUID
import spray.json.DefaultJsonProtocol
import spray.httpx.SprayJsonSupport

case class Android(os: String, ui: String)
case class Battery(standbyTime: String, talkTime: String, batteryType: String)
case class Camera(features: List[String], primary: String)
case class Connectivity(bluetooth: String, cell: String, gps: Boolean, infrared: Boolean, wifi: String)
case class Display(screenResolution: String, screenSize: String, touchScreen: Boolean)
case class Hardware(accelerometer: Boolean, audioJack: String, cpu: String, fmRadio: Boolean, physicalKeyboard: Boolean, usb: String)
case class SizeAndWeight(dimensions: List[String], weight: String)
case class Storage(flash: String, ram: String)
case class Device(additionalFeatures: String, android: Android, availability: List[String], battery: Battery, camera: Camera, connectivity: Connectivity, description: String, display: Display, hardware: Hardware, id: String, images: List[String], name: String, sizeAndWeight: SizeAndWeight, storage: Storage)
case class DeviceMetaData(age: Double, id: String, imageUrl: String, name: String, snippet: String)
trait ProductJsonSerializers extends DefaultJsonProtocol with SprayJsonSupport {
  implicit val androidFormat = jsonFormat2(Android.apply)
  implicit val batteryFormat = jsonFormat3(Battery.apply)
  implicit val connectivityFormat = jsonFormat5(Connectivity.apply)
  implicit val displayFormat = jsonFormat3(Display.apply)
  implicit val hardwareFormat = jsonFormat6(Hardware.apply)
  implicit val cameraFormat = jsonFormat2(Camera.apply)
  implicit val sAndWFormat = jsonFormat2(SizeAndWeight.apply)
  implicit val storageFormat = jsonFormat2(Storage.apply)
  implicit val deviceFormat = jsonFormat14(Device.apply)
  implicit val deviceMetaDataFormat = jsonFormat5(DeviceMetaData.apply)
}

