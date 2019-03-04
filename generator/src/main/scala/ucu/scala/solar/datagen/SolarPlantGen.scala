package ucu.scala.solar.datagen

import java.util.Properties

import messageProtocols.SolarPanelData
import org.apache.kafka.clients.producer.{KafkaProducer, ProducerRecord}

import scala.collection.immutable

class SolarPlantGen(val location: String,
                    val plantId: String,
                    val panelNumber: Int,
                    messagePeriod: Long,
                    properties: Properties,
                    topic: String) {
    
    
    val tasks: immutable.IndexedSeq[Runnable] = for(i <- 1 to panelNumber) yield new Runnable {
        def run(): Unit = {
            val panel = new SolarPanelGen(
                location = location,
                plantId = plantId,
                panelId = i.toString
            )
            val producer = new KafkaProducer[String, SolarPanelData](properties)
            
            while(true) {
                val data = panel.generateSensorData()
                
                producer.send(new ProducerRecord[String, SolarPanelData](topic, data))
                
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


