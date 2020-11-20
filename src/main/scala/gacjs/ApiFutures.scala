package gacjs

import cats.effect.Async
import cats.effect.Concurrent
import cats.syntax.flatMap._
import cats.syntax.traverse._
import com.google.api.core.ApiFuture
import com.google.api.core.ApiFutureCallback
import com.google.api.core.{ApiFutures => GoogleApiFutures}
import com.google.common.util.concurrent.MoreExecutors

object ApiFutures {

  def async[F[_], A](future: F[ApiFuture[A]])(implicit F: Async[F]): F[A] =
    future >>= (fu => F.async[A](addCallback(fu, _)))

  def traverse[F[_], A](futures: Vector[F[ApiFuture[A]]])(implicit F: Async[F]): F[Vector[A]] =
    futures.traverse(async[F, A])

  def cancelable[F[_], A](future: F[ApiFuture[A]])(implicit F: Concurrent[F]): F[A] =
    future >>= (fu => F.cancelable[A] { cb => addCallback(fu, cb); F.delay { fu.cancel(true); () } })

  def traverseCancelable[F[_], A](futures: Vector[F[ApiFuture[A]]])(implicit F: Concurrent[F]): F[Vector[A]] =
    futures.traverse(cancelable[F, A])

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
