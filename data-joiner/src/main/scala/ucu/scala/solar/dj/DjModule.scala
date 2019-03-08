package ucu.scala.solar.dj

import java.util.Properties

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
    
    
    val djModuleConfig = DjModuleConfig()
    
    val streamJoiner = new DjModule(djModuleConfig)
    streamJoiner.start()
}
