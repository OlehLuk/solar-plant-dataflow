package ucu.scala.solar.datagen

case class PlantConfig(
                      locationName: String,
                      plantId: String,
                      panelNumber: Int,
                      messagePeriod: Long
                      )

case class GeneratorModuleConfig(sensorTopic: String = "sensors",
                                    appName: String = "sensor-generation",
                                    kafkaEndpoint: String = "localhost:9092",
                                    plantsConfig: List[PlantConfig] = Nil
                                )
