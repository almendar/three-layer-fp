package threelayer
package realworld.storage
import java.nio.file._

import cats.Monad
import cats.data.ReaderT
import cats.effect.IO
import threelayer.realworld.storage.StorageMonad.DataHolder

abstract class FileStorage[A] { self =>
  def run: FileExecEnv[A]

  def map[B](f: A => B): FileStorage[B] = new FileStorage[B] {
    override def run: FileExecEnv[B] = self.run.map(f)
  }

  def flatMap[B](f: A => FileStorage[B]): FileStorage[B] = new FileStorage[B] {
    override def run: FileExecEnv[B] = self.run.flatMap(a => f(a).run)
  }
}

object FileStorage {
  implicit def monadInstance: Monad[FileStorage] = new Monad[FileStorage] {
    override def pure[A](x: A): FileStorage[A] = new FileStorage[A] {
      override def run: FileExecEnv[A] = (_: Unit) => IO.pure(x)
    }
    override def flatMap[A, B](fa: FileStorage[A])(f: A => FileStorage[B]): FileStorage[B] = fa.flatMap(f)

    override def tailRecM[A, B](a: A)(f: A => FileStorage[Either[A, B]]): FileStorage[B] = ???
  }

  implicit def instance: StorageMonad[FileStorage] = new StorageMonad[FileStorage] {

    override def save(path: StorageMonad.Location, dh: StorageMonad.DataHolder): FileStorage[Long] =
      new FileStorage[Long] {
        override def run: FileExecEnv[Long] = ReaderT { _ =>
          IO.delay(Files.copy(dh.stream, Paths.get(path.a), StandardCopyOption.REPLACE_EXISTING))

        }
      }
    override def read(path: StorageMonad.Location): FileStorage[StorageMonad.DataHolder] =
      new FileStorage[StorageMonad.DataHolder] {
        override def run: FileExecEnv[StorageMonad.DataHolder] = ReaderT { _ =>
          IO.delay(DataHolder.apply(Paths.get(path.a)))
        }
      }
  }
}
