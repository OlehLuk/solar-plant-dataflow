package ucu.scala.solar.datagen

import java.util.Properties

import net.manub.embeddedkafka.{EmbeddedKafka, EmbeddedKafkaConfig}
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.common.serialization.StringDeserializer
import org.apache.kafka.streams.StreamsConfig
import org.scalatest.{BeforeAndAfterAll, FlatSpec}

class DataGenSpec extends FlatSpec with EmbeddedKafka with BeforeAndAfterAll {
    val topic = "sensors"
    val props: Properties = {
        val p = new Properties()
        p.put(StreamsConfig.APPLICATION_ID_CONFIG, "generator")
        p.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092")
        p.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest")
        p.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer")
        p.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer")
        p
    }
    
    implicit val config: EmbeddedKafkaConfig =
        EmbeddedKafkaConfig(kafkaPort = 9092, zooKeeperPort = 2182)
    
    it should "publish synchronously data to kafka" in {
        val producer = new DataGen(props)
        producer.produce(topic, List(List("KEY","THIS IS MESSAGE")))
        val response = consumeFirstStringMessageFrom(topic)
        assert(Some(response).isDefined)
    }
    
    it should "publish proper key-value's to kafka" in {
        val producer = new DataGen(props)
        producer.produce(topic, List(
            List("Lviv", "Alles Gut")
        ))
        val response = consumeFirstKeyedMessageFrom[String,String](topic)(
            config, new StringDeserializer, new StringDeserializer
        )
        assert(Some(response).isDefined)
        assert(response._1 === "Lviv")
        assert(response._2 === "Alles Gut")
        println(response)
    }
    
    override def beforeAll(): Unit = {
        EmbeddedKafka.start()
    }
    
    override def afterAll(): Unit = {
        EmbeddedKafka.stop()
    }
}
