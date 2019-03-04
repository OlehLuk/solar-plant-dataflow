package ucu.scala.solar.datagen

import java.util.Properties

import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.streams.StreamsConfig

import scala.collection.immutable

object LumosMaxima extends App {
    //format of config: plant location, plant id, number of panels,
    // ... timeout between messages in milliseconds
    val plantsConfiguration = List(
        List("London", 1, 3, 1000),
        List("California", 2, 3, 1000),
        List("Lviv", 3, 3, 1000),
        List("London", 4, 3, 1000)
    )
    
    val props: Properties = {
        val p = new Properties()
        p.put(StreamsConfig.APPLICATION_ID_CONFIG, "generator")
        p.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092")
        p.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest")
        p.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer")
        p.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer")
        p
    }
    
    val topic = "sensorData"
    
    
    
    val plants: immutable.Seq[SolarPlantGen] = for(plantConfig <- plantsConfiguration) yield {
    //TODO: use some other structure to store configuration/read it from file/stdin
    // to get rid of these ugly .asInstanceOf
        new SolarPlantGen(plantConfig(0).asInstanceOf[String],
            plantConfig(1).asInstanceOf[String],
            plantConfig(2).asInstanceOf[Int],
            plantConfig(3).asInstanceOf[Long],
            props, topic)
    }
    
    for (plant <- plants) {
        plant.startPanelsGeneration()
    }


}
