package messageSerdes

import java.io.{ByteArrayInputStream, ByteArrayOutputStream, ObjectInputStream, ObjectOutputStream}
import java.{util => j_util}

import org.apache.kafka.common.serialization.{Deserializer, Serde, Serializer}

class GenericMessageSerializer[T] extends Serializer[T] {
  override def configure(map: j_util.Map[String, _], b: Boolean): Unit = {}

  override def serialize(s: String, data: T): Array[Byte] = {
    try {
      val byteOut = new ByteArrayOutputStream()
      val objOut = new ObjectOutputStream(byteOut)
      objOut.writeObject(data)
      objOut.close()
      byteOut.close()
      byteOut.toByteArray
    }
    catch {
      case ex: Exception => throw new Exception(ex.getMessage)
    }
  }

  override def close(): Unit = {}
}

class GenericMessageDeserializer[T] extends Deserializer[T] {
  override def configure(map: j_util.Map[String, _], b: Boolean): Unit = {}

  override def deserialize(s: String, bytes: Array[Byte]): T = {
    val byteIn = new ByteArrayInputStream(bytes)
    val objIn = new ObjectInputStream(byteIn)
    val obj = objIn.readObject().asInstanceOf[T]
    byteIn.close()
    objIn.close()
    obj
  }

  override def close(): Unit = {}
}

class GenericMessageSerde[T]  extends Serde[T] {
  override def configure(map: j_util.Map[String, _], b: Boolean): Unit = {}

  override def close(): Unit = {}

  override def serializer(): Serializer[T] = new GenericMessageSerializer()

  override def deserializer(): Deserializer[T] = new GenericMessageDeserializer()
}

/*
  Exampe of usage:
  import messageProtocols.WeatherData
  val weatherDataSerde: Serde[WeatherData] = new GenericMessageSerde[WeatherData]()
 */
