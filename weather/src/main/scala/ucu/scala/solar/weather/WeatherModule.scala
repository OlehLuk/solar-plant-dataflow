package ucu.scala.solar.weather

import java.util.Properties
import java.util.concurrent.{ScheduledThreadPoolExecutor, TimeUnit}

import appConfig._
import common.MessageProducer
import messageProtocols.WeatherData

import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.streams.StreamsConfig

import scala.util.{Failure, Success, Try}

class WeatherModule[T](config: WeatherModuleConfig,
                       messageProducer: MessageProducer[T],
                       wDaemon: Weather[T]
                      ) {

    val executor = new ScheduledThreadPoolExecutor(1)

    val task = new Runnable {
        def run() = {
            LOCATIONS_SET.foreach(l => getWeather(l))
        }
    }

    val action = executor.scheduleAtFixedRate(task, 1, config.EXECUTE_PERIOD, TimeUnit.SECONDS)
    //    action.cancel(false)

    def getWeather(location: String): Unit = {

        val weatherResp: Try[(String,T)] = wDaemon.getWeatherForLocation(location)
        weatherResp match {
            case Success((k: String, wData:T)) =>
                messageProducer.produceSingle(config.TOPIC_NAME, (k, wData))
            case Failure(e) => println(e)
        }
    }
}


object WeatherModule extends App {
    val moduleConfigs = new WeatherModuleConfig()
    val wDaemon = new WeatherDaemon(moduleConfigs)

    val messageProducerProps = {
        val p = new Properties()
        p.put(StreamsConfig.APPLICATION_ID_CONFIG, "generator")
        p.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092")
        p.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest")
        p
    }
    val messageProducer = new MessageProducer[WeatherData](messageProducerProps)

    val wModule = new WeatherModule[WeatherData](moduleConfigs, messageProducer, wDaemon)
}
