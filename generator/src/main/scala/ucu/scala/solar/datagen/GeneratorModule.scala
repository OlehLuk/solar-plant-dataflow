package ucu.scala.solar.datagen

import java.util.Properties

import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.streams.StreamsConfig

import scala.collection.immutable

class GeneratorModule(val config: GeneratorModuleConfig) {
    val plantsConfiguration = List(
        List("London", 1, 3, 1000),
        List("California", 2, 3, 1000),
        List("Lviv", 3, 3, 1000),
        List("London", 4, 3, 1000)
    )
    
    val props: Properties = {
        val p = new Properties()
        p.put(StreamsConfig.APPLICATION_ID_CONFIG, config.appName)
        p.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, config.kafkaEndpoint)
        p.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest")
        p
    }
    
    val topic = "sensorData"
    
    val plants: immutable.Seq[SolarPlantGen] = for(plantConfig <- plantsConfiguration) yield {
        new SolarPlantGen(plantConfig(0).asInstanceOf[String],
            plantConfig(1).asInstanceOf[String],
            plantConfig(2).asInstanceOf[Int],
            plantConfig(3).asInstanceOf[Long],
            props, config.sensorTopic)
    }
    
    def start() = {
        for (plant <- plants) {
            plant.startPanelsGeneration()
        }
    }
}

object GeneratorModule extends App {
    val config = new GeneratorModuleConfig()
    val module = new GeneratorModule(config)
    module.start()
}
