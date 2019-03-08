package ucu.scala.solar.dj

import java.util.Properties

import common.{ConfigReader, Read}
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.streams.{KafkaStreams, StreamsConfig}

class DjModule(djModuleConfig: DjModuleConfig) {
    val djConfig: Properties = {
        val p = new Properties()
        p.put(StreamsConfig.APPLICATION_ID_CONFIG, djModuleConfig.appName)
        p.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, djModuleConfig.kafkaEndpoint)
        p.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest")
        p
    }
    val djStreams = new KafkaStreams(new MartinGarrix().jam(
        djModuleConfig.weatherTopic, djModuleConfig.sensorTopic,
        djModuleConfig. joinedTopic, djModuleConfig.weatherUpdatePeriod), djConfig)
    
    def start(): Unit = djStreams.start()
}

object DjModule extends App {
    val filepath: String = args(0)

    implicit object ReadDJConfigs extends Read[DjModuleConfig] {
        def read(argsAsStr: Array[String]): DjModuleConfig = {
            val Array(wTopic, sTopic, jTopic, updPeriod, appName, kafkaEndPoint) = argsAsStr.map(_.split(",")(0))
            new DjModuleConfig(wTopic, sTopic, jTopic, updPeriod.toInt, appName, kafkaEndPoint)
        }
    }
    val moduleConfigs: DjModuleConfig = ConfigReader[DjModuleConfig](filepath)
    
    val streamJoiner = new DjModule(moduleConfigs)
    streamJoiner.start()
}
