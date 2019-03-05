package messageProtocols

//String for location - temporary.
// - Why? I don't see any need for introducing both location id and location name at this point

case class WeatherData(
                          timestamp: Long, // will be part of the key for joining weather stream
                 //timestamp should be somehow normalized for joining streams
                          locationName: String, // will be part of the key for joining weather stream
                          locationTemperature: Float = 0,
                          locationHumidity: Int = 0,
                          locationPressure: Int = 0,
                          locationCloudiness: Int = 0,
                      ) {
    //just for better testing/logging. Could be removed or refactored
    override def toString: String = timestamp + " " + locationName

    override def equals(obj: Any): Boolean = obj match {
        case obj: WeatherData =>
            timestamp == obj.timestamp &&
                locationName == obj.locationName &&
                locationTemperature == obj.locationTemperature &&
                locationHumidity == obj.locationHumidity &&
                locationPressure == obj.locationPressure &&
                locationCloudiness == obj.locationCloudiness
        case _ => false
    }
}
