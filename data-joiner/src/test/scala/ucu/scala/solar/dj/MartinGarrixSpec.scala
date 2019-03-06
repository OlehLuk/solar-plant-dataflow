package ucu.scala.solar.dj

import java.util.Properties

import messageProtocols.{DjData, SolarPanelData, WeatherData}
import messageSerdes.GenericMessageSerde
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.common.serialization.{StringDeserializer, StringSerializer}
import org.apache.kafka.streams.{StreamsConfig, TopologyTestDriver}
import org.apache.kafka.streams.test.ConsumerRecordFactory
import org.scalatest.FlatSpec

class MartinGarrixSpec extends FlatSpec{
    val sensorTopic = "sensors"
    val weatherTopic = "weather"
    val joinedTopic = "mash-up"
    
    implicit val solarPanelDataSerde: GenericMessageSerde[SolarPanelData] =
        new GenericMessageSerde[SolarPanelData]
    implicit val weatherDataSerde: GenericMessageSerde[WeatherData] =
        new GenericMessageSerde[WeatherData]
    implicit val djDataSerde: GenericMessageSerde[DjData] =
        new GenericMessageSerde[DjData]
    
    val factorySensorData = new ConsumerRecordFactory[String, SolarPanelData](sensorTopic,
        new StringSerializer, solarPanelDataSerde.serializer())
    val factoryWeather = new ConsumerRecordFactory[String, WeatherData](weatherTopic,
        new StringSerializer, weatherDataSerde.serializer())
    
    val testClass = new MartinGarrix()
    
    it should "join messages properly in synchronous mode" in {
        // When
        val testDriver = new TopologyTestDriver(
            testClass.jam(weatherTopic, sensorTopic, joinedTopic, 10), config)
        
        
        testWeatherData.foreach(entry => testDriver.pipeInput(
            factoryWeather.create(entry)))
        testSensorData.foreach(entry => testDriver.pipeInput(
            factorySensorData.create(entry)))
        
        // Then
        assertValue(1, true)
        assertValue(2, true)
        assertValue(3, true)
        assertValue(3, true)
        assertValue(5, false)
        
        
        def assertValue(expected: Int, joined: Boolean): Unit = {
            val received = testDriver.readOutput(joinedTopic,
                new StringDeserializer, djDataSerde.deserializer()).value()
            println(received)
            if (joined) {
                assert(received.panelTemperature === received.locationTemperature)
                assert(received.panelTemperature === expected)
            } else {
                assert(received.locationTemperature === -1)
                assert(received.panelTemperature === expected)
            }
        }
    }
    
    
    
    val config: Properties = {
        val p = new Properties()
        p.put(StreamsConfig.APPLICATION_ID_CONFIG, "test-bestdj-app")
        p.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "dummy:1234")
        p.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest")
        p
    }
    
    val now: Long = 100*1000//Calendar.getInstance().getTimeInMillis
    
    val testSensorData = List(
        SolarPanelData(now+1, "Lviv", "1", "1", 1, 1, 1),
        SolarPanelData(now-10*1000+2, "Lviv", "1", "1", 2, 2, 2),
        SolarPanelData(now-15*1000+3, "Lviv", "1", "1", 3, 3, 3),
        SolarPanelData(now-20*1000+4, "Lviv", "1", "1", 3, 3, 3),
        SolarPanelData(now-40*1000+5, "Lviv", "1", "1", 5, 5, 5),
        
    )
    val testWeatherData = List(
        WeatherData(now+1, "Lviv", 1 , 1, 1, 1),
        WeatherData(now-10*1000+2, "Lviv", 2,2,2,2),
        WeatherData(now-20*1000+3, "Lviv", 3,3,3,3),
        WeatherData(now-30*1000+4, "Lviv", 4,4,4,4)
    )
}