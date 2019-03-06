package messageProtocols

/* We will join weather info and panel info by:
    * formatting keys of both streams as following : `location-normalized_timestamp`
    * joining them
   There are 2 questions here:
    * How to normalize timestamps? My offer - set timestamp at the beginning of the corresponding second.
    That is how often we update weather.
    * Will kafka be happy with a lot of messages that have the same key?
 */
case class DjData(panelId: String,
             plantId: String,
             //timestamp should be somehow normalized for joining streams
             timestamp: Long, // but after joining streams this should be timestamp of sensor data
             locationName: String,
             locationTemperature: Float = -1,
             locationHumidity: Int = -1,
             locationPressure: Int = -1,
             locationCloudiness: Int = -1,
             panelVoltage: Double, // for performance monitoring
             panelCurrent: Double,  // for performance monitoring
             panelTemperature: Double // for predictive maintenance / malfunction mitigation
        ) {
        
        def this(panel: SolarPanelData, weather: WeatherData) {
                this(
                        panelId = panel.panelId,
                        plantId = panel.plantId,
                        timestamp = panel.timestamp,
                        locationName = panel.location,
                        panelVoltage = panel.panelVoltage,
                        panelCurrent = panel.panelCurrent,
                        panelTemperature = panel.panelTemperature,
                        locationTemperature = weather.locationTemperature,
                        locationHumidity = weather.locationHumidity,
                        locationCloudiness = weather.locationCloudiness,
                        locationPressure = weather.locationPressure
                )
        }
        
        def this(panel: SolarPanelData) {
                this(
                        panelId = panel.panelId,
                        plantId = panel.plantId,
                        timestamp = panel.timestamp,
                        locationName = panel.location,
                        panelVoltage = panel.panelVoltage,
                        panelCurrent = panel.panelCurrent,
                        panelTemperature = panel.panelTemperature
                )
        }
        
        }
