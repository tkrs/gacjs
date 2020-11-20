package gacjs

import java.util.concurrent.CompletableFuture
import java.util.concurrent.Executor
import java.util.concurrent.TimeUnit

import cats.effect.IO
import com.google.api.core.ApiFuture
import com.google.api.core.{ApiFutures => GoogleApiFutures}
import munit.FunSuite

import scala.concurrent.ExecutionContext

class ApiFuturesTest extends FunSuite {
  test("async() should retrieve the passed future's value") {
    val io = ApiFutures.async(IO(GoogleApiFutures.immediateFuture(10)))
    assertEquals(io.unsafeRunSync(), 10)
  }

  test("async() should fail when the passed future was failed") {
    val io = ApiFutures.async[IO, Int](IO(GoogleApiFutures.immediateFailedFuture(new Exception("oh no"))))
    interceptMessage[Exception]("oh no")(io.unsafeRunSync())
  }

  test("traverse() should retrieve passed future's values") {
    val io = ApiFutures.traverse[IO, Int](Vector.range(0, 10).map(a => IO(GoogleApiFutures.immediateFuture(a))))
    assertEquals(io.unsafeRunSync(), Vector.range(0, 10))
  }

  test("traverse() should fail when passed futures has failed future") {
    val io = ApiFutures.traverse[IO, Int](
      Vector
        .range(0, 10)
        .map(a =>
          IO(
            if (a == 7) GoogleApiFutures.immediateFailedFuture[Int](new Exception("Hmm"))
            else GoogleApiFutures.immediateFuture(a)
          )
        )
    )
    interceptMessage[Exception]("Hmm")(io.unsafeRunSync())
  }

  test("cancelable() should retrieve the passed future's value") {
    implicit val cs = IO.contextShift(ExecutionContext.global)
    val io          = ApiFutures.cancelable[IO, Int](IO(GoogleApiFutures.immediateFuture(10)))
    assertEquals(io.unsafeRunSync(), 10)
  }

  test("cancelable() should fail when the passed future was failed") {
    implicit val cs = IO.contextShift(ExecutionContext.global)
    val io          = ApiFutures.cancelable[IO, Int](IO(GoogleApiFutures.immediateFailedFuture(new Exception("oh no"))))
    interceptMessage[Exception]("oh no")(io.unsafeRunSync())
  }

  test("traverseCancelable() should retrieve passed future's values") {
    implicit val cs = IO.contextShift(ExecutionContext.global)
    val io =
      ApiFutures.traverseCancelable[IO, Int](Vector.range(0, 10).map(a => IO(GoogleApiFutures.immediateFuture(a))))
    assertEquals(io.unsafeRunSync(), Vector.range(0, 10))
  }

  test("traverseCancelable() should fail when passed futures has failed future") {
    implicit val cs = IO.contextShift(ExecutionContext.global)
    val io = ApiFutures.traverseCancelable[IO, Int](
      Vector
        .range(0, 10)
        .map(a =>
          IO(
            if (a == 7) GoogleApiFutures.immediateFailedFuture[Int](new Exception("Hmm"))
            else GoogleApiFutures.immediateFuture(a)
          )
        )
    )
    interceptMessage[Exception]("Hmm")(io.unsafeRunSync())
  }

  test("traverseCancelable() should cancel when passed futures has failed future") {
    implicit val cs = IO.contextShift(ExecutionContext.global)
    val never       = newNever[Int]
    val io = ApiFutures.traverseCancelable[IO, Int](
      Vector
        .range(0, 10)
        .map(a =>
          IO(
            if (a == 7) never
            else GoogleApiFutures.immediateFuture(a)
          )
        )
    )
    val cancel = io.unsafeRunCancelable(cb => fail(s"got result: $cb"))
    assert(!never.isCancelled)

    cancel.unsafeRunSync()

    assert(never.isCancelled)
  }

  def newNever[A]: ApiFuture[A] = new ApiFuture[A] {
    private val inner = CompletableFuture.supplyAsync[A] { () => Thread.sleep(Long.MaxValue); ??? }

    override def addListener(listener: Runnable, executor: Executor): Unit = inner.thenRunAsync(listener, executor)
    override def cancel(mayInterruptIfRunning: Boolean): Boolean           = inner.cancel(mayInterruptIfRunning)
    override def isCancelled: Boolean                                      = inner.isCancelled()
    override def isDone: Boolean                                           = inner.isDone()
    override def get(): A                                                  = inner.get()
    override def get(timeout: Long, unit: TimeUnit): A                     = inner.get(timeout, unit)
  }
}
