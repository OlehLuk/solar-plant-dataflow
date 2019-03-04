package messageProtocols


class SolarPanelData(val timestamp: Long, // will be part of the key for joining
                     val location: String, // will be part of the key for joining
                     val panelId: String, // will NOT be part of the key for joining weather stream
                     val plantId: String, // will NOT be part of the key for joining
                     val panelVoltage: Float, // for performance monitoring
                     val panelCurrent: Float,  // for performance monitoring
                     val panelTemperature: Float // for predictive maintenance / malfunction mitigation
                    ) {}
