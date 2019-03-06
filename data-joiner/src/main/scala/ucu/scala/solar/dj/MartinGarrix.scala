package ucu.scala.solar.dj

import java.util.concurrent.TimeUnit

import messageProtocols.{DjData, SolarPanelData, WeatherData}
import messageSerdes.GenericMessageSerde
import org.apache.kafka.streams.Topology
import org.apache.kafka.streams.kstream.JoinWindows
import org.apache.kafka.streams.scala.ImplicitConversions._
import org.apache.kafka.streams.scala._
import org.apache.kafka.streams.scala.kstream._

class MartinGarrix {
    def jam(): Topology = {
        import org.apache.kafka.streams.scala.Serdes._
        
        val builder: StreamsBuilder = new StreamsBuilder
        
        implicit val solarPanelDataSerde: GenericMessageSerde[SolarPanelData] =
            new GenericMessageSerde[SolarPanelData]
        implicit val weatherDataSerde: GenericMessageSerde[WeatherData] =
            new GenericMessageSerde[WeatherData]
        implicit val djDataSerde: GenericMessageSerde[DjData] =
            new GenericMessageSerde[DjData]
        
        val weatherUpdatePeriod = 10 //seconds
        
        def normalizeTimestamp(t: Long): Long =
            t - (t % (weatherUpdatePeriod*1000))
        
        
        val sensorData: KStream[String, SolarPanelData] =
            builder.stream[String, SolarPanelData]("sensors-4")
                .selectKey((_, data) => data.location + ":" + normalizeTimestamp(data.timestamp))
        val weather: KStream[String, WeatherData] =
            builder.stream[String, WeatherData]("weather-4")
                .selectKey((_, data) => data.locationName + ":" + normalizeTimestamp(data.timestamp))
        
        
        
        
        sensorData.leftJoin(weather)((sensor,weather) => {
            if (weather != null ) {
                println("join found" + normalizeTimestamp(sensor.timestamp))
                new DjData(sensor, weather)
            }
            else {
                println("join not found" + normalizeTimestamp(sensor.timestamp))
                new DjData(sensor)
            }
        }
            ,
            JoinWindows.of(TimeUnit.MINUTES.toMillis(5))).to("mash-up-4")
        
        builder.build()
    }
}
