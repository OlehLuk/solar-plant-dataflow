package common

trait Read[A] { def read(s: Array[String]): A }

object ConfigReader {
    def apply[A: Read](filename: String): A = {
        val bufferedSource = io.Source.fromFile(filename)
        //remove "," and no matter if configs are line by line or single line. Only order matters
        val argsAsStr = bufferedSource.getLines().mkString("").split(",")
        implicitly[Read[A]].read(argsAsStr)
    }
}
