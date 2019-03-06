package messageProtocols


case class SolarPanelData(timestamp: Long, // will be part of the key for joining
                     location: String, // will be part of the key for joining
                     panelId: String, // will NOT be part of the key for joining weather stream
                     plantId: String, // will NOT be part of the key for joining
                     panelVoltage: Double, // for performance monitoring
                     panelCurrent: Double,  // for performance monitoring
                     panelTemperature: Double // for predictive maintenance / malfunction mitigation
                    ) {}
