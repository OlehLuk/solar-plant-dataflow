package ucu.scala.solar.weather

import java.util.Date

import play.api.libs.json._
import scalaj.http.Http

import scala.util.{Failure, Try}


//Custom imports
import messageProtocols.WeatherData

trait Weather[T] {
    def getWeatherForLocation(l: String): Try[(String, T)]
}

case class WeatherDaemon(moduleConfig: WeatherModuleConfig) extends Weather[WeatherData] {
    def getWeatherForLocation(location: String): Try[(String, WeatherData)] = {
        val unit = "metric"
        val appID = moduleConfig.APP_ID
        val weatherProviderURL = moduleConfig.WEATHER_PROVIDER_URL

        val connTimeout = 60 * 1000
        val readTimeout = 60 * 1000

        requestWeather(location, weatherProviderURL, unit, appID, connTimeout, readTimeout)
    }

    def requestWeather(location: String, weatherProviderURL: String,
                       unit: String, appID: String, connTimeout: Int, readTimeout: Int): Try[(String,WeatherData)] = {
        val response = Http(weatherProviderURL)
            .param("q", location)
            .param("units", unit)
            .param("appid", appID)
            .timeout(connTimeout, readTimeout).asString

        if (response.isSuccess) {
            Try {
                handleWeatherRequest(response.body)
            }
        }
        else {
            Failure(new Error("Request failed."))
        }
    }

    def handleWeatherRequest(request: String): (String,WeatherData) = {

        val response_body: JsValue = Json.parse(request)

        val location = (response_body \ "list" \ 0 \ "name").get.toString().stripPrefix("\"").stripSuffix("\"")
        val temperature = (response_body \ "list" \ 0 \ "main" \ "temp").get.toString().toFloat
        val humidity = (response_body \ "list" \ 0 \ "main" \ "humidity").get.toString().toInt
        val pressure = (response_body \ "list" \ 0 \ "main" \ "pressure").get.toString().toInt
        val cloudiness = (response_body \ "list" \ 0 \ "clouds" \ "all").get.toString().toInt

        val now = new Date().getTime
        val result = (location, new WeatherData(
            timestamp = now,
            locationName = location,
            locationTemperature = temperature,
            locationHumidity = humidity,
            locationPressure = pressure,
            locationCloudiness = cloudiness
        ))
        result
    }
}

//TODO: Merge WeatherDaemon and WeatherGen. Result should be able to:
// * take list of locations' names as input arguments
// * get weather data for these locations each second/any time period you define
// * write it to Kafka
// * run forever

//https://stackoverflow.com/questions/11719373/doing-http-request-in-scala
//https://github.com/scalaj/scalaj-http
//https://openweathermap.org/current
//https://www.programcreek.com/scala/scalaj.http.Http
//https://www.playframework.com/documentation/2.0/ScalaJson
//https://darksky.net/dev/docs/libraries#python-library
//https://rapidapi.com/weatherbit/api/weather
//https://www.wunderground.com/member/favorites