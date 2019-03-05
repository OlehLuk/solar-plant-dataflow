package ucu.scala.solar.weather

class WeatherModuleConfig(
                             val TOPIC_NAME: String = "weather_data",
                             val WEATHER_PROVIDER_URL: String = "https://samples.openweathermap.org/data/2.5/find",
                             val APP_ID: String = "",
                             val EXECUTE_PERIOD: Int = 10

                         ) {}
