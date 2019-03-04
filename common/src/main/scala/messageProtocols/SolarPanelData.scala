package messageProtocols


class SolarPanelData(val timestamp: Long, // will be part of the key for joining
                     val location: String, // will be part of the key for joining
                     val panelId: String, // will NOT be part of the key for joining weather stream
                     val plantId: String, // will NOT be part of the key for joining
                     val panelVoltage: Double, // for performance monitoring
                     val panelCurrent: Double,  // for performance monitoring
                     val panelTemperature: Double // for predictive maintenance / malfunction mitigation
                    ) {}
