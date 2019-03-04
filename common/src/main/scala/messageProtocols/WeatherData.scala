package messageProtocols

//String for location - temporary.
// - Why? I don't see any need for introducing both location id and location name at this point

class WeatherData(val timestamp: Long, // will be part of the key for joining weather stream
                 //timestamp should be somehow normalized for joining streams
                  val locationName: String, // will be part of the key for joining weather stream
                  val locationTemperature: Float,
                  val locationHumidity: Int,
                  val locationPressure: Int,
                  val locationCloudiness: Int,
                 ) {}
