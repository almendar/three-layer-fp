package threelayer
package realworld.storage
import cats.Monad
import cats.data.ReaderT
import cats.effect.IO
import threelayer.realworld.storage.StorageMonad.DataHolder

abstract class S3Storage[A] { self =>

  def run: S3ExecEnv[A]

  def map[B](f: A => B): S3Storage[B] = new S3Storage[B] {
    override def run: S3ExecEnv[B] = self.run.map(f)
  }

  def flatMap[B](f: A => S3Storage[B]): S3Storage[B] = new S3Storage[B] {
    override def run: S3ExecEnv[B] = self.run.flatMap(a => f(a).run)
  }
}

object S3Storage {
  implicit def monadInstance: Monad[S3Storage] = new Monad[S3Storage] {

    override def pure[A](x: A): S3Storage[A] = new S3Storage[A] {
      override def run: S3ExecEnv[A] = (_: S3Client) => IO.pure(x)
    }
    override def flatMap[A, B](fa: S3Storage[A])(f: A => S3Storage[B]): S3Storage[B] = fa.flatMap(f)
    override def tailRecM[A, B](a: A)(f: A => S3Storage[Either[A, B]]): S3Storage[B] = ???
  }

  implicit def instance: StorageMonad[S3Storage] = new StorageMonad[S3Storage] {
    override def save(path: StorageMonad.Location, dh: StorageMonad.DataHolder): S3Storage[Long] =
      new S3Storage[Long] {
        override def run: S3ExecEnv[Long] = ReaderT { env =>
          IO.delay {
            println(s"Storing input stream $dh at location $path")
            100L
          }
        }
      }

    override def read(path: StorageMonad.Location): S3Storage[StorageMonad.DataHolder] =
      new S3Storage[StorageMonad.DataHolder] {
        override def run: S3ExecEnv[StorageMonad.DataHolder] = ReaderT { env =>
          IO.delay {
            val dataHolder: StorageMonad.DataHolder = DataHolder.apply(s"This is content of file from s3 path: ${path.a}")
            println(s"Downloading from path $path ")
            dataHolder
          }
        }
      }
  }
}
