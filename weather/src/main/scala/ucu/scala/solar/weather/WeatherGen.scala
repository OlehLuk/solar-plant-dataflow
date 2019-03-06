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
            producer.send(new ProducerRecord[String, T](topic, m._1, m._2))
        }
        producer.close(100L, TimeUnit.MILLISECONDS)
    }

    def produceSingle(topic: String, message: (String, T)): Unit = {
        val sendFuture: Future[RecordMetadata] =
            producer.send(new ProducerRecord[String, T](topic, message._1, message._2))
    }

    def closeProducer(): Unit = {
        producer.close()
    }
}

