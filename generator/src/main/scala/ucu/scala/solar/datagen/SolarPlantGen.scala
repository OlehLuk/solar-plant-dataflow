package ucu.scala.solar.datagen

import java.util.Properties

import common.MessageProducer
import messageProtocols.SolarPanelData

import scala.collection.immutable

class SolarPlantGen(val location: String,
                    val plantId: String,
                    val panelNumber: Int,
                    messagePeriod: Long,
                    properties: Properties,
                    topic: String) {
    
    
    val tasks: immutable.IndexedSeq[Runnable] = for(i <- 1 to panelNumber) yield new Runnable {
        def run(): Unit = {
            val panelDataGenerator = new SolarPanelGen(
                location = location,
                plantId = plantId,
                panelId = i.toString
            )
            val messageProducer = new MessageProducer[SolarPanelData](properties)
            
            while(true) {
                val data = panelDataGenerator.generateSensorData()
                val msgKey = location+plantId+i.toString
                messageProducer.produceSingle(topic, (msgKey, data))
                println("MESSAGE PUSHED TO KAFKA")
                println(data)
                Thread.sleep(messagePeriod)
            }
            
        }
    }
    
    def startPanelsGeneration(): Unit = {
        for(task <- tasks) {
           new Thread(task).start()
        }
    }
    
}


