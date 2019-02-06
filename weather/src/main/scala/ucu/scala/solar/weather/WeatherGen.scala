package ucu.scala.solar.weather

import java.util.Properties
import java.util.concurrent.TimeUnit

import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.producer._
import org.apache.kafka.streams.StreamsConfig

class WeatherGen(properties: Properties) {
    val producer = new KafkaProducer[String, String](properties)
        def produce(topic: String, messages: List[List[String]]): Unit = {
        messages.foreach { m =>
            producer.send(new ProducerRecord[String, String](topic, m(0), m(1)))
        }
        producer.close(100L, TimeUnit.MILLISECONDS)
    }
}


object WeatherGen extends App{
    val props: Properties = {
        val p = new Properties()
        p.put(StreamsConfig.APPLICATION_ID_CONFIG, "generator")
        p.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092")
        p.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest")
        p.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer")
        p.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer")
        p
    }
    
    val generator = new WeatherGen(props)
    generator.produce("sensors", List(
        List("Lviv", "Alles Gut"),
        List("Sumy", "Winter is comming"),
        List("London", "is the capital of Great Britain!"),
    ))
}
