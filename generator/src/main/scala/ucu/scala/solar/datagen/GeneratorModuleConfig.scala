package ucu.scala.solar.datagen

case class GeneratorModuleConfig(sensorTopic: String = "sensors",
                                    appName: String = "sensor-generation",
                                    kafkaEndpoint: String = "localhost:9092"
                                )
