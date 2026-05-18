package solver14mv.solver

import cats.MonadThrow
import cats.effect.Async
import cats.syntax.all.*
import fs2.Stream
import fs2.concurrent.Channel
import solver14mv.solver.SolveEvent.CellSafety

trait Solver[F[_]] {

  /** Solve the puzzle by checking every `(i, j)` that `filter(i, j)` is true */
  def solve(
      input: Solver.Input,
      filter: (Int, Int) => Boolean,
  ): Stream[F, SolveEvent]

  def solve(input: Solver.Input): Stream[F, SolveEvent] =
    solve(input, (_, _) => true)
}

object Solver {
  final case class Input(
      clues: IArray[IArray[Clue]],
      rules: Set[Rule],
      mineCount: Option[Int],
  )

  def direct[F[_]: Async]: Solver[F] = new Solver[F] {
    override def solve(
        input: Input,
        filter: (Int, Int) => Boolean,
    ): Stream[F, SolveEvent] = {
      val Input(clues, rules, mineCount) = input // scalafix:ok
      val m = clues.length
      val n = clues.lift(0).map(_.length).getOrElse(0)

      val ij = for {
        i <- 0 until m
        j <- 0 until n
        if filter(i, j)
      } yield (i, j)

      val model: F[Model[F]] = Model(clues, rules, mineCount)
      val eventChannel: F[Channel[F, SolveEvent]] =
        Channel.unbounded[F, SolveEvent]

      object UnsatError extends Exception

      val pendingEvent = SolveEvent.Pending(ij.toSet)
      val check: Stream[F, SolveEvent] =
        Stream(pendingEvent) ++ Stream
          .eval((model, eventChannel).tupled)
          .flatMap { case (model, eventChannel) =>
            val solving: Stream[F, Nothing] = Stream(ij*)
              .parEvalMapUnordered(8) { case (i, j) =>
                val result: F[SolveEvent] = model.solve(i, j).flatMap {
                  case SolveResult.Safe =>
                    SolveEvent.Result(i, j, CellSafety.Safe).pure
                  case SolveResult.Mine =>
                    SolveEvent.Result(i, j, CellSafety.Mine).pure
                  case SolveResult.Indeterminate =>
                    SolveEvent.Result(i, j, CellSafety.Indeterminate).pure
                  case SolveResult.Unsat => MonadThrow[F].raiseError(UnsatError)
                }
                val sendBeginEvent =
                  eventChannel.send(SolveEvent.Begin(i, j)).void
                val sendResultEvent = result.flatMap(eventChannel.send).void
                sendBeginEvent *> sendResultEvent
              }
              .handleErrorWith { case UnsatError =>
                Stream.exec(eventChannel.send(SolveEvent.Unsat).void)
              }
              .drain
            eventChannel.stream.mergeHaltBoth(solving)
          }

      check
    }
  }

  def incremental[F[_]: Async]: F[Solver[F]] = IncrementalSolver[F]
}
