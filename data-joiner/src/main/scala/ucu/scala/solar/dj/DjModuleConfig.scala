package ucu.scala.solar.dj

case class DjModuleConfig (
    weatherTopic: String = "weather",
    sensorTopic: String = "sensors",
    joinedTopic: String = "mash-up",
    weatherUpdatePeriod: Int = 5,
    appName: String = "weather-sensor-join",
    kafkaEndpoint: String = "localhost:9092"
)
