package ucu.scala.solar.weather

import java.util.Properties

import common.MessageProducer
import messageProtocols.WeatherData
import messageSerdes.GenericMessageDeserializer

import net.manub.embeddedkafka.{EmbeddedKafka, EmbeddedKafkaConfig}
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.common.serialization.StringDeserializer
import org.apache.kafka.streams.StreamsConfig
import org.scalatest.{BeforeAndAfterAll, FlatSpec}

class MessageProducerSpec extends FlatSpec with EmbeddedKafka with BeforeAndAfterAll {
    val topic = "sensors"
    val weatherDataDeserializer = new GenericMessageDeserializer[WeatherData]()
    val props: Properties = {
        val p = new Properties()
        p.put(StreamsConfig.APPLICATION_ID_CONFIG, "generator")
        p.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092")
        p.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest")
        p
    }
    
    implicit val config: EmbeddedKafkaConfig =
        EmbeddedKafkaConfig(kafkaPort = 9092, zooKeeperPort = 2182)
    
    it should "publish synchronously data to kafka" in {
        val producer = new MessageProducer[WeatherData](props)
        producer.produce(topic, List(("KEY",new WeatherData(1,"THIS IS MESSAGE"))))
        val response = consumeFirstStringMessageFrom(topic)
        assert(Some(response).isDefined)
    }
    
    it should "publish proper key-value's to kafka" in {
        val producer = new MessageProducer[WeatherData](props)
        val msg = new WeatherData(1, "Alles Gut")
        producer.produce(topic, List(
            ("Lviv", msg)
        ))
        val response = consumeFirstKeyedMessageFrom[String,WeatherData](topic)(
            config, new StringDeserializer, weatherDataDeserializer
        )
        assert(Some(response).isDefined)
        assert(response._1 === "Lviv")
        assert(response._2.equals(msg))
        println(response)
    }
    
    override def beforeAll(): Unit = {
        EmbeddedKafka.start()
    }
    
    override def afterAll(): Unit = {
        EmbeddedKafka.stop()
    }
}
