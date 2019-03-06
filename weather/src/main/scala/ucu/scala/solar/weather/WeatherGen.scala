package ucu.scala.solar.weather
import java.util.Properties
import java.util.concurrent.{Future, TimeUnit}

import messageSerdes.GenericMessageSerializer
import org.apache.kafka.clients.producer._
import org.apache.kafka.common.serialization.StringSerializer

class WeatherGen[T](properties: Properties) {
    val weatherDataSerializer = new GenericMessageSerializer[T]()
    val producer = new KafkaProducer[String, T](properties, new StringSerializer(), weatherDataSerializer)

    def produce(topic: String, messages: List[(String, T)]): Unit = {
        messages.foreach { m =>
            println(m._2.toString)
            println("GONNA SEND")
            producer.send(new ProducerRecord[String, T](topic, m._1, m._2))
        }
        producer.close(100L, TimeUnit.MILLISECONDS)
    }

    def produceSingle(topic: String, message: (String, T)): Unit = {
        println(message._2)
        println("SEND TO KAFKA")
        val sendFuture: Future[RecordMetadata] =
            producer.send(new ProducerRecord[String, T](topic, message._1, message._2))
    }

    def closeProducer(): Unit = {
        producer.close()
    }
}


object WeatherGen extends App {
    //    println("dsf")

//    val props: Properties = {
//        val p = new Properties()
//        p.put(StreamsConfig.APPLICATION_ID_CONFIG, "generator")
//        p.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092")
//        p.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest")
////        p.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer")
////        p.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer")
//        p
//    }
//
//    val generator = new WeatherGen[WeatherData](props)
//    generator.produce("sensors", List(
//        ("Lviv", new WeatherData(1, "Alles Gut")),
//        ("Sumy", new WeatherData(2,"Winter is comming")),
//        ("London", new WeatherData(3,"is the capital of Great Britain!")),
//    ))
}
