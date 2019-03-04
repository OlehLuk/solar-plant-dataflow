package messageProtocols

/* We will join weather info and panel info by:
    * formatting keys of both streams as following : `location-normalized_timestamp`
    * joining them
   There are 2 questions here:
    * How to normalize timestamps? My offer - set timestamp at the beginning of the corresponding second.
    That is how often we update weather.
    * Will kafka be happy with a lot of messages that have the same key?
 */
class DjData(val panelId: String,
             val plantId: String,
             //timestamp should be somehow normalized for joining streams
             val timestamp: Long, // but after joining streams this should be timestamp of sensor data
             val locationName: String,
             val locationTemperature: Float,
             val locationHumidity: Int,
             val locationPressure: Int,
             val locationCloudiness: Int,
             val panelVoltage: Float, // for performance monitoring
             val panelCurrent: Float,  // for performance monitoring
             val panelTemperature: Float // for predictive maintenance / malfunction mitigation
        ) {
        
        def this(panel: SolarPanelData, weather: WeatherData) {
                this(
                        panelId = panel.panelId,
                        plantId = panel.plantId,
                        timestamp = panel.timestamp,
                        locationName = panel.location,
                        locationTemperature = weather.locationTemperature,
                        locationHumidity = weather.locationHumidity,
                        locationCloudiness = weather.locationCloudiness,
                        locationPressure = weather.locationPressure,
                        panelVoltage = panel.panelVoltage,
                        panelCurrent = panel.panelCurrent,
                        panelTemperature = panel.panelTemperature
                )
        }
        
        }
