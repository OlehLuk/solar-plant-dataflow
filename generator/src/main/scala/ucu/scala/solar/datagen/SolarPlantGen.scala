package ucu.scala.solar.datagen

import java.util.Properties

import messageProtocols.SolarPanelData
import messageSerdes.GenericMessageSerializer
import org.apache.kafka.clients.producer.{KafkaProducer, ProducerRecord}
import org.apache.kafka.common.serialization.StringSerializer

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
            val solarPanelDataSerializer = new GenericMessageSerializer[SolarPanelData]
            val producer = new KafkaProducer[String, SolarPanelData](properties,
                new StringSerializer, solarPanelDataSerializer)
            
            while(true) {
                val data = panel.generateSensorData()
                
                producer.send(new ProducerRecord[String, SolarPanelData](topic,
                    location+plantId+i.toString, data))
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


