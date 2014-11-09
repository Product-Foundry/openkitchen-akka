package com.xebia.openkitchen

import java.util.UUID

import spray.json.DefaultJsonProtocol
object ProductDomain {
  case class Android(os: String, ui: String)

  case class Battery(standbyTime: String, talkTime: String, batteryType: String)
  case class Camera(features: List[String], primary: String)
  case class Connectivity(bluetooth: String, cell: String, gps: Boolean, infrared: Boolean, wifi: String)
  case class Display(screenResolution: String, screenSize: String, touchScreen: Boolean)
  case class Hardware(accelerometer: Boolean, audioJack: String, cpu: String, fmRadio: Boolean, physicalKeyboard: Boolean, usb: String)
  case class SizeAndWeight(dimensions: List[String], weight: String)
  case class Storage(flash: String, ram: String)
  case class Device(additionalFeatures: String, android: Android, availability: List[String], battery: Battery, camera: Camera, connectivity: Connectivity, description: String, display: Display, hardware: Hardware, id: String, images: List[String], name: String, sizeAndWeight: SizeAndWeight, storage: Storage)

}


