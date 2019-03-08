package ucu.scala.solar.datagen

import java.util.Properties

import messageProtocols.SolarPanelData
import messageSerdes.GenericMessageDeserializer
import net.manub.embeddedkafka.{EmbeddedKafka, EmbeddedKafkaConfig}
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.streams.StreamsConfig
import org.scalatest.{BeforeAndAfterAll, FlatSpec}

class SolarPlantGenSpec extends FlatSpec with EmbeddedKafka with BeforeAndAfterAll {
    val topic = "sensors"
    val props: Properties = {
        val p = new Properties()
        p.put(StreamsConfig.APPLICATION_ID_CONFIG, "generator")
        p.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092")
        p.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest")
        p
    }
    val solarPanelDataDeserializer = new GenericMessageDeserializer[SolarPanelData]
    
    implicit val config: EmbeddedKafkaConfig =
        EmbeddedKafkaConfig(kafkaPort = 9092, zooKeeperPort = 2182)
    
    it should "publish data to kafka" in {
        val plant = new SolarPlantGen("London", "1", 1, 1000,
            props, topic)
        
        plant.startPanelsGeneration()
        Thread.sleep(2000)
        val response = consumeFirstMessageFrom(topic)(config, solarPanelDataDeserializer)
        assert(Some(response).isDefined)
        println(response)
    }
    
    it should "publish data to kafka from multiple streams simultaneously" in {
        val plant = new SolarPlantGen("London", "1", 2, 1000,
            props, topic)
        
        plant.startPanelsGeneration()
        Thread.sleep(2000)
        val response = consumeFirstMessageFrom(topic)(config, solarPanelDataDeserializer)
        assert(Some(response).isDefined)
        println(response)
    }
    
    
    override def beforeAll(): Unit = {
        EmbeddedKafka.start()
    }
    
    override def afterAll(): Unit = {
        EmbeddedKafka.stop()
    }
}
