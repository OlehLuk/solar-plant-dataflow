package ucu.scala.solar.datagen

import java.util.Date

import messageProtocols.SolarPanelData

class SolarPanelGen(val location: String,
                    val plantId: String,
                    val panelId: String) {
    
    private val seed = (location+plantId+panelId).hashCode
    private val r = new scala.util.Random(seed)
    
    def generateSensorData(): SolarPanelData = {
        new SolarPanelData(
            timestamp = new Date().getTime,
            location= location,
            panelId = panelId.toString,
            plantId = plantId,
            panelVoltage = r.nextGaussian()*2 + 12,
            panelCurrent = r.nextGaussian()*0.5 + 10,
            panelTemperature = r.nextGaussian()*3+20
        )
    }
}
