# solar-plant-dataflow [![Build Status](https://travis-ci.com/OlehLuk/solar-plant-dataflow.svg?token=wiznHxovdwoge1SZ3KTJ&branch=master)](https://travis-ci.com/OlehLuk/solar-plant-dataflow)
### Dataflow management for the simulated solar plant
Developed as final project for "Functional and Streaming Programing in Scala" course at UCU.


### Design and implementation

The project consists of several modules:

1. Common – utilities code;
2. Weather –  retrieving weather data from API for the  location;
3. Data generator – for simulation of solar panels data;
4. DJ (for data-joiner) – Kafka streams merging.


#### Common
In the common module, we have implemented utility code to push messages into Kafka and generic message Serde’s for pushing custom objects into Kafka.


#### Weather
Weather module implements logic for retrieving information from the weather provider API, we’ve chosen openweathermap.org as weather provider. Every given period of time for all specified locations it sends appropriate HTTP requests to the API endpoint and pushes retrieved response to Kafka.

Configuration of the module includes:
* Weather API endpoint;
* API credentials;
* locations;
* Kafka app name, endpoint & topic names, where info is written to;
* the time interval to request & update weather data.


#### Data generator
This module implements an intelligent logic of three sensors’ data generation: panel voltage, current and temperature of hardware. It also implements management of these panels at one plant and creation of several plants. Each panel is running in the separate thread, so they generate sensor data and pushes it to Kafka in parallel.

Configuration of the module includes:
* number of plants and their start config: location, plant id, number of panels.
* Kafka app name, endpoint & topic info is written to.


#### Data joiner 
This module merges Kafka streams – weather and sensor streams. To join streams we use location and timestamp as a key. We used left join to join weather to sensor data, as we designed our system on the assumption that plants data is the most critical for us and it’s better to lose information about whether if it’s not present, rather than provide inconsistent one. 

Configuration of the module includes:
* Kafka app name, endpoint & topic names, where info is read & written to.
* weather update interval.

Common configuration like Kafka topic names, endpoints, weather update interval, message format are shared between modules.


### Scalability
Scaling of solar plant data generation is done by running several applications that simulate information from different plants.
Scaling of weather provider is not really necessary at this number of locations. However, it also can be scaled by distributing weather applications for different locations.
DJ module functionality of merging streams can be scaled as any other Kafka application.


### Testing
Implementation of  modules’ features are covered with unit tests including usage of EmbeddedKafka.
In DJ module [IntegrationTest.scala](https://github.com/OlehLuk/solar-plant-dataflow/blob/master/data-joiner/src/test/scala/ucu/scala/solar/dj/IntegrationTest.scala) can be found. It implements integration tests for the whole pipeline. This can be used both to check that modules run at all and that modules work properly. Messages posted to Kafka are copied in the stdout, so you can check what it is posting. In particular, results of join are outputted as well. So, it’s easy to check if joining is working. 
However, if you want to run project parts the easiest way is to pack jars for each module and run it with the required configuration. 
Entry points for module programs are 
1. [WeatherModule.scala](https://github.com/OlehLuk/solar-plant-dataflow/blob/master/weather/src/main/scala/ucu/scala/solar/weather/WeatherModule.scala) for weather;
2. [GeneratorModule.scala](https://github.com/OlehLuk/solar-plant-dataflow/blob/master/generator/src/main/scala/ucu/scala/solar/datagen/GeneratorModule.scala) for data generator;
3. [DjModule.scala](https://github.com/OlehLuk/solar-plant-dataflow/blob/master/data-joiner/src/main/scala/ucu/scala/solar/dj/DjModule.scala) for data joiner.
