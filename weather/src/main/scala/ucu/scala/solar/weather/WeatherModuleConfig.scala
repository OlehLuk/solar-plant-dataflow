package ucu.scala.solar.weather

class WeatherModuleConfig(
                             val TOPIC_NAME: String = "weather_data",
                             val WEATHER_PROVIDER_URL: String = "https://openweathermap.org/data/2.5/find",
                             val APP_ID: String = "b6907d289e10d714a6e88b30761fae22",
                             val EXECUTE_PERIOD: Int = 10

                         ) {}
