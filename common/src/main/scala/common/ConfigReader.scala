package common

trait Read[A] { def read(s: Array[String]): A }

object ConfigReader {
    def apply[A: Read](filename: String): A = {
        val bufferedSource = io.Source.fromFile(filename)
        val argsAsStr = bufferedSource.getLines
        implicitly[Read[A]].read(argsAsStr.toArray)
    }
}
