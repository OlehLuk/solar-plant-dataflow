package ucu.scala.solar.datagen

import java.util.Properties

import common.{ConfigReader, Read}
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.streams.StreamsConfig

import scala.collection.immutable

class GeneratorModule(val config: GeneratorModuleConfig) {
    
    val props: Properties = {
        val p = new Properties()
        p.put(StreamsConfig.APPLICATION_ID_CONFIG, config.appName)
        p.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, config.kafkaEndpoint)
        p.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest")
        p
    }
    
    val topic = "sensorData"
    
    val plants: immutable.Seq[SolarPlantGen] = for(plantConfig <- config.plantsConfig) yield {
        new SolarPlantGen(plantConfig.locationName,
            plantConfig.plantId,
            plantConfig.panelNumber,
            plantConfig.messagePeriod,
            props, config.sensorTopic)
    }
    
    def start() = {
        for (plant <- plants) {
            plant.startPanelsGeneration()
        }
    }
}

object GeneratorModule extends App {
    val filepath: String = args(0)

    implicit object ReadGeneratorConfig extends Read[GeneratorModuleConfig] {
        def read(argsAsStr: Array[String]): GeneratorModuleConfig = {
            val topicName = argsAsStr(0).stripSuffix(",")
            val appName = argsAsStr(1).stripSuffix(",")
            val kafkaEndPoint = argsAsStr(2).stripSuffix(",")
            val nPlants = argsAsStr(3).stripSuffix(",")
            val plantsConfigs = for (i <- 0 until nPlants.toInt) yield {
                val Array(location, plantId, panelNumber, messagePeriod) = argsAsStr(4 + i).split(",").map(_.trim)
                PlantConfig(location, plantId, panelNumber.toInt, messagePeriod.toLong)
            }
            new GeneratorModuleConfig(topicName, appName, kafkaEndPoint, plantsConfigs.toList)
        }
    }
    val moduleConfigs: GeneratorModuleConfig = ConfigReader[GeneratorModuleConfig](filepath)
    val module = new GeneratorModule(moduleConfigs)
    module.start()
}
