package ucu.scala.solar.dj

import java.util.Properties

import messageProtocols.{DjData, SolarPanelData, WeatherData}
import messageSerdes.{GenericMessageDeserializer, GenericMessageSerde}
import net.manub.embeddedkafka.{EmbeddedKafka, EmbeddedKafkaConfig}
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.common.serialization.StringSerializer
import org.apache.kafka.streams.{KafkaStreams, StreamsConfig}
import org.scalatest.{BeforeAndAfterAll, FlatSpec}
import ucu.scala.solar.datagen.SolarPlantGen
import ucu.scala.solar.weather.{WeatherDaemon, WeatherGen, WeatherModule, WeatherModuleConfig}

class IntegrationTest extends FlatSpec with EmbeddedKafka with BeforeAndAfterAll {
    
    val solarPanelDataDeserializer = new GenericMessageDeserializer[SolarPanelData]
    val weatherDataDeserializer = new GenericMessageDeserializer[WeatherData]
    val djDataDeserializer = new GenericMessageDeserializer[DjData]
    
    implicit val config: EmbeddedKafkaConfig =
        EmbeddedKafkaConfig(kafkaPort = 9092, zooKeeperPort = 2182)
    
    val sensorTopic = "sensors"
    val weatherTopic = "weather"
    val joinedTopic = "mash-up"
    
    //create weather producer
    val weatherGenProps = {
        val p = new Properties()
        p.put(StreamsConfig.APPLICATION_ID_CONFIG, "weatherGenerator")
        p.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092")
        p.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest")
        p
    }
    
    val moduleConfigs = new WeatherModuleConfig(TOPIC_NAME = weatherTopic,
        APP_ID="", EXECUTE_PERIOD = 5)
    val messageProducer = new WeatherGen[WeatherData](weatherGenProps)
    val wDaemon = new WeatherDaemon(moduleConfigs)
    
    
    
    
    //create plants
    val plantProps: Properties = {
        val p = new Properties()
        p.put(StreamsConfig.APPLICATION_ID_CONFIG, "plantGenerator")
        p.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092")
        p.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest")
        p
    }
    
    val plantLondon = new SolarPlantGen("London", "1", 2, 1000,
        plantProps, sensorTopic)
    val plantLviv = new SolarPlantGen("Lviv", "2", 2, 1000,
        plantProps, sensorTopic)
    
    //DJ
    val djConfig: Properties = {
        val p = new Properties()
        p.put(StreamsConfig.APPLICATION_ID_CONFIG, "test-bestdj-app")
        p.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092")
        p.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest")
        p
    }
    val djStreams = new KafkaStreams(new MartinGarrix().jam(
        weatherTopic, sensorTopic, joinedTopic, 5), djConfig)
    
    
    //manual weather data
    val manWprops: Properties = {
        val p = new Properties()
        p.put(StreamsConfig.APPLICATION_ID_CONFIG, "manual-weather")
        p.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092")
        p.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest")
        p
    }
    implicit val weatherDataSerde: GenericMessageSerde[WeatherData] =
        new GenericMessageSerde[WeatherData]
    val producer = new KafkaProducer[String, WeatherData](manWprops,
        new StringSerializer, weatherDataSerde.serializer())
    
    
    //test
    it should "work" in {
        djStreams.start()
        //here i start weather module. and it doesn't join to the sensor stream, gets suspicious data
        val wModule = new WeatherModule[WeatherData](moduleConfigs, messageProducer, wDaemon)
        
        //here i manually write weather. it joins.
        /*val now: Long = Calendar.getInstance().getTimeInMillis
        println("now is " + now)
        val testWeatherData = List(
            WeatherData(now+1, "Lviv", 1 , 1, 1, 1),
            WeatherData(now+5*1000+2, "Lviv", 2,2,2,2),
            WeatherData(now+5*2*1000+3, "Lviv", 3,3,3,3),
            WeatherData(now+5*3*1000+4, "Lviv", 4,4,4,4)
        )
        
        for (w<-testWeatherData) {
            producer.send(new ProducerRecord[String, WeatherData](weatherTopic,
                "1", w))
        }*/
        
        
        plantLondon.startPanelsGeneration()
        plantLviv.startPanelsGeneration()
        
        Thread.sleep(15000)
        
        val response = consumeFirstMessageFrom(joinedTopic)(config, djDataDeserializer)
        assert(Some(response).isDefined)
        println(response)
    }
    
    
    
    
    
    
    
    //val response = consumeFirstMessageFrom(sensorTopic)(config, solarPanelDataDeserializer)
    //println(response)
    
    override def beforeAll(): Unit = {
        EmbeddedKafka.start()
    }
    
    override def afterAll(): Unit = {
        EmbeddedKafka.stop()
    }
}
