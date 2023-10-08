#!/usr/bin/env -S scala-cli shebang -q

//> using scala "3.3.1"
//> using jvm "zulu:21"
//> using toolkit typelevel:latest

import cats.syntax.all.*
import cats.effect.*
import scala.concurrent.duration.*

val THREADS_COUNT = 10000

def measureTime[A](task: IO[A]): IO[A] =
  for {
    startTs <- IO.monotonic
    r <- task
    endTs <- IO.monotonic
    _ <- IO.println(s"Time: ${(endTs - startTs).toMillis} ms")
  } yield r

inline def virtualThread[A](inline block: A): IO[A] =
  IO.async { cb =>
    IO {
      val th = Thread.ofVirtual().start { () =>
        try cb(Right(block))
        catch case e: Throwable => cb(Left(e))
      }
      Some(IO {
        th.interrupt()
        th.join()
      })
    }
  }

def testWithBlocking: IO[Unit] =
  (0 until THREADS_COUNT).toList
    .parTraverse(_ => IO.interruptible(Thread.sleep(1000)))
    .void

def testWithVirtualThreads: IO[Unit] =
  (0 until THREADS_COUNT).toList
    .parTraverse(_ => virtualThread(Thread.sleep(1000)))
    .void

object App extends IOApp:
  def run(args: List[String]): IO[ExitCode] =
    for {
      _ <- IO.println("Starting...")
      // _ <- measureTime(testWithBlocking)
      _ <- measureTime(testWithVirtualThreads)
    } yield ExitCode.Success

