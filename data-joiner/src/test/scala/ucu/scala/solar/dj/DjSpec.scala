package ucu.scala.solar.dj

import java.util.Properties

import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.common.serialization.{StringDeserializer, StringSerializer}
import org.apache.kafka.streams.test.ConsumerRecordFactory
import org.apache.kafka.streams.{StreamsConfig, TopologyTestDriver}
import org.scalatest.FlatSpec

class DjSpec extends FlatSpec{
    
    val factorySensorData = new ConsumerRecordFactory[String, String]("sensors",
        new StringSerializer, new StringSerializer)
    val factoryWeather = new ConsumerRecordFactory[String, String]("weather",
        new StringSerializer, new StringSerializer)
    val testClass = new DJ()
    
    it should "convert each line of text to the length of that text" in {
        // When
        val testDriver = new TopologyTestDriver(testClass.jam(), config)
        testSensorData.foreach(entry => testDriver.pipeInput(
            factorySensorData.create("sensors", entry(0) , entry(1))))
        
        testWeatherData.foreach(entry => testDriver.pipeInput(
            factoryWeather.create("weather", entry(0) , entry(1))))
        
        // Then
        assertValue(26)
        assertValue(19)
        assertValue(58)
        assertValue(27)
        
        assertResult(null)(testDriver.readOutput("mash-up"))
        
        def assertValue(expected: Int): Unit = {
            val received = testDriver.readOutput("mash-up",
                new StringDeserializer, new StringDeserializer)
    
            println(received)
        }
    }
    
    
    
    val config: Properties = {
        val p = new Properties()
        p.put(StreamsConfig.APPLICATION_ID_CONFIG, "test-dj-app")
        p.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "dummy:1234")
        p.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest")
        p
    }
    
    val now: Long = 1//Calendar.getInstance().getTimeInMillis
    
    val testSensorData = List(
        List("1", "Alles Gut"),
        List("2", "Alles Gut"),
        List("3", "Snow in Hell"),
        List("4", "Burning Man"),
    )
    val testWeatherData = List(
        List("1", "+15"),
        List("2", "+20"),
        List("3", "-100"),
        List("4", "+100"),
    )
}
