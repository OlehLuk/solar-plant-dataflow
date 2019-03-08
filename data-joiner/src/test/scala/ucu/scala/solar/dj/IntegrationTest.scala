package ucu.scala.solar.dj

import java.util.Properties

import common.MessageProducer
import messageProtocols.{DjData, SolarPanelData, WeatherData}
import messageSerdes.{GenericMessageDeserializer, GenericMessageSerde}
import net.manub.embeddedkafka.{EmbeddedKafka, EmbeddedKafkaConfig}
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.streams.{KafkaStreams, StreamsConfig}
import org.scalatest.{BeforeAndAfterAll, FlatSpec}
import ucu.scala.solar.datagen.SolarPlantGen
import ucu.scala.solar.weather.{WeatherDaemon, WeatherModule, WeatherModuleConfig}

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
    
    val moduleConfigs = new WeatherModuleConfig(
        TOPIC_NAME = weatherTopic,
        APP_ID="b6907d289e10d714a6e88b30761fae22",
        EXECUTE_PERIOD = 5
    )
    val messageProducer = new MessageProducer[WeatherData](weatherGenProps)
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
    
    //test
    it should "work" in {
        djStreams.start()
        val wModule = new WeatherModule[WeatherData](moduleConfigs, messageProducer, wDaemon)
        
        plantLondon.startPanelsGeneration()
        plantLviv.startPanelsGeneration()

        Thread.sleep(15000)
        
        val response = consumeFirstMessageFrom(joinedTopic)(config, djDataDeserializer)
        assert(Some(response).isDefined)
        println(response)
    }
    
    override def beforeAll(): Unit = {
        EmbeddedKafka.start()
    }
    
    override def afterAll(): Unit = {
        EmbeddedKafka.stop()
    }
}
