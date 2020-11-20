package gacjs

import cats.effect.IO
import cats.syntax.flatMap._
import cats.syntax.traverse._
import com.google.api.core.ApiFuture
import com.google.api.core.ApiFutureCallback
import com.google.api.core.{ApiFutures => GoogleApiFutures}
import com.google.common.util.concurrent.MoreExecutors

object ApiFutures {

  def async[A](future: IO[ApiFuture[A]]): IO[A] =
    future >>= (fu => IO.async(addCallback(fu, _)))

  def traverse[A](futures: Vector[IO[ApiFuture[A]]]): IO[Vector[A]] =
    futures.traverse(async)

  def cancelable[A](future: IO[ApiFuture[A]]): IO[A] =
    future >>= (fu => IO.cancelable[A] { cb => addCallback(fu, cb); IO(fu.cancel(true)) })

  def traverseCancelable[A](futures: Vector[IO[ApiFuture[A]]]): IO[Vector[A]] =
    futures.traverse(cancelable)

  private def addCallback[A](future: ApiFuture[A], callback: Either[Throwable, A] => Unit): Unit =
    GoogleApiFutures.addCallback(
      future,
      new ApiFutureCallback[A] {
        def onFailure(e: Throwable): Unit = callback(Left(e))
        def onSuccess(v: A): Unit         = callback(Right(v))
      },
      MoreExecutors.directExecutor()
    )
}
