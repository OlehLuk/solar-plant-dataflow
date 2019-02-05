import java.util.Properties

import org.apache.kafka.streams.kstream.Materialized
import org.apache.kafka.streams.scala.ImplicitConversions._
import org.apache.kafka.streams.scala._
import org.apache.kafka.streams.scala.kstream._
import org.apache.kafka.streams.{KafkaStreams, StreamsConfig}

object WordCountScala extends App {
    import Serdes._
    val props: Properties = {
        val p = new Properties()
        p.put(StreamsConfig.APPLICATION_ID_CONFIG, "word-count-scala")
        p.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092")
        p
    }

    val builder: StreamsBuilder = new StreamsBuilder
    val textLines: KStream[String, String] = builder.stream[String, String]("word-count-input")
    val wordCounts: KTable[String, Long] = textLines
        .mapValues(_.toLowerCase)
        .flatMapValues(_.split("\\W+"))
        .selectKey((_, word) => word)
        .groupByKey
        .count()(Materialized.as("Counts"))

    wordCounts.toStream.to("word-count-output")

    val topology = builder.build()
    println(topology.describe)

    val streams: KafkaStreams = new KafkaStreams(topology, props)
    streams.cleanUp()
    streams.start()

    sys.ShutdownHookThread {streams.close()}
}
