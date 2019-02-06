package ucu.scala.solar.dj

import java.util.Properties
import java.util.concurrent.TimeUnit

import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.streams.kstream.JoinWindows
import org.apache.kafka.streams.scala.ImplicitConversions._
import org.apache.kafka.streams.scala._
import org.apache.kafka.streams.scala.kstream._
import org.apache.kafka.streams.{KafkaStreams, StreamsConfig, Topology}

class DJ {
    
    
    def jam(): Topology = {
        import org.apache.kafka.streams.scala.Serdes._
        
        val builder: StreamsBuilder = new StreamsBuilder
        
        val sensorData: KStream[String, String] = builder.stream[String, String]("sensors")
        val weather: KStream[String, String] = builder.stream[String, String]("weather")
        
        sensorData.join(weather)((v1,v2) => v1+";"+v2,
            JoinWindows.of(TimeUnit.MINUTES.toMillis(5))).to("mash-up")
        
        builder.build()
    }
}

object DJ extends App {
    val props: Properties = {
        val p = new Properties()
        p.put(StreamsConfig.APPLICATION_ID_CONFIG, "dj")
        p.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092")
        p.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest")
        p
    }
    
    val app = new DJ
    val topology = app.jam()
    
    println(topology.describe)
    
    val streams: KafkaStreams = new KafkaStreams(topology, props)
    
    streams.cleanUp()
    streams.start()
    
    sys.ShutdownHookThread {
        streams.close()
    }
}
