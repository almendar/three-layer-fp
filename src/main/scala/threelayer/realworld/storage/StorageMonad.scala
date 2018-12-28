package threelayer
package realworld.storage

import java.io.{FileInputStream, InputStream}
import java.nio.file.Path

import cats.Monad
import com.sun.xml.internal.messaging.saaj.util.ByteInputStream
import threelayer.realworld.storage.StorageMonad.{DataHolder, Location}

object StorageMonad {
  final case class Location(a: String)

  abstract class DataHolder extends AutoCloseable {
    type Holder
    protected val h: Holder
    def stream: InputStream
    override def close(): Unit = stream.close()
  }

  object DataHolder {
    def apply(bytes: Array[Byte]): DataHolder = new DataHolder {
      override type Holder = Array[Byte]
      override protected val h: Array[Byte] = bytes
      override def stream: InputStream = new ByteInputStream(h, h.length)
    }

    def apply(path: Path): DataHolder = new DataHolder {
      override type Holder = java.io.File
      override protected val h = path.toFile
      override def stream: InputStream = new FileInputStream(h)
    }

    def apply(s: String): DataHolder = new DataHolder {
      override type Holder = String
      override protected val h: String = s
      override def stream: InputStream = new ByteInputStream(h.getBytes, h.getBytes.length)
    }
  }
}

abstract class StorageMonad[F[_]: Monad] {
  def save(path: Location, dh: DataHolder): F[Long]
  def read(path: Location): F[DataHolder]
}
