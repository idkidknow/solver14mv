package solver14mv.solver

import cats.effect.Async
import cats.syntax.all.*
import fs2.Stream
import solver14mv.solver.IncrementalSolver.State
import cats.effect.std.Mutex
import cats.effect.kernel.Sync
import solver14mv.solver.SolveEvent.CellSafety
import solver14mv.solver.Solver.Input

class IncrementalSolver[F[_]: Async] private (mutex: Mutex[F])
    extends Solver[F] {

  // used inside mutex.lock
  private var state: State =
    State(Solver.Input(IArray.empty, Set(), None), Array.empty)
  private def getState: F[State] = Sync[F].delay(state)
  private def setState(newState: State): F[Unit] =
    Sync[F].delay { state = newState }
  private def updateStateResults(event: SolveEvent): F[Unit] = Sync[F].delay {
    event match {
      case SolveEvent.Result(i, j, CellSafety.Safe) =>
        state.result(i)(j) = SolveResult.Safe
      case SolveEvent.Result(i, j, CellSafety.Mine) =>
        state.result(i)(j) = SolveResult.Mine
      case _ =>
    }
  }

  private val directSolver: Solver[F] = Solver.direct[F]

  private def mn(input: Solver.Input) =
    (input.clues.length, input.clues.lift(0).map(_.length).getOrElse(0))

  private def canPatch(
      prevInput: Solver.Input,
      input: Solver.Input,
  ): Boolean = {
    val (m, n) = mn(input)
    if (
      mn(prevInput) =!= (m, n)
      || prevInput.rules =!= input.rules
      || prevInput.mineCount =!= input.mineCount
    ) false
    else {
      val prevClues = prevInput.clues
      val currClues = input.clues
      (for {
        i <- 0 until m
        j <- 0 until n
      } yield (prevClues(i)(j), currClues(i)(j))).forall {
        case (Clue.None, _) => true
        case (Clue.QuestionMark, Clue.None | Clue.Flagged) => false
        case (Clue.QuestionMark, _) => true
        case (prev, curr) if prev === curr => true
        case _ => false
      }
    }
  }

  override def solve(
      input: Solver.Input,
      filter: (Int, Int) => Boolean,
  ): Stream[F, SolveEvent] = {
    Stream.resource(mutex.lock).flatMap { _ =>
      val (m, n) = mn(input)
      val patch: F[(Input, Map[(Int, Int), SolveResult])] = for {
        currState <- getState
        newState <- canPatch(currState.input, input) match {
          case false =>
            val newState =
              State(input, Array.fill(m, n)(SolveResult.Indeterminate))
            setState(newState).as(newState)
          case true =>
            val newClues = {
              val arr = input.clues.map(_.toSeq.toArray).toSeq.toArray
              for {
                i <- 0 until m
                j <- 0 until n
              } {
                currState.result(i)(j) match {
                  case SolveResult.Mine => arr(i)(j) = Clue.Flagged
                  case SolveResult.Safe if arr(i)(j) === Clue.None =>
                    arr(i)(j) = Clue.QuestionMark
                  case _ =>
                }
              }
              IArray.unsafeFromArray(arr.map(IArray.unsafeFromArray))
            }
            val newInput = input.copy(clues = newClues)
            val newState = currState.copy(input = newInput)
            setState(newState).as(newState)
        }
      } yield {
        val skipped = (for {
          i <- 0 until m
          j <- 0 until n
          if newState.result(i)(j) =!= SolveResult.Indeterminate
        } yield ((i, j), newState.result(i)(j))).toMap
        (newState.input, skipped)
      }

      Stream
        .eval(patch)
        .flatMap { case (input, skipped) =>
          val skippedEvents = skipped.map { case ((i, j), result) =>
            result match {
              case SolveResult.Indeterminate =>
                SolveEvent.Result(i, j, CellSafety.Indeterminate)
              case SolveResult.Mine => SolveEvent.Result(i, j, CellSafety.Mine)
              case SolveResult.Safe => SolveEvent.Result(i, j, CellSafety.Safe)
              case SolveResult.Unsat => SolveEvent.Unsat
            }
          }.toSeq
          val others = directSolver.solve(
            input,
            (i, j) => filter(i, j) && !skipped.contains((i, j)),
          )
          if (skippedEvents.exists(_ === SolveEvent.Unsat)) Stream(SolveEvent.Unsat)
          else Stream(skippedEvents*) ++ others
        }
        .evalTap(updateStateResults)
    }
  }
}

object IncrementalSolver {
  private final case class State(
      input: Solver.Input,
      result: Array[Array[SolveResult]],
  )

  def apply[F[_]: Async]: F[Solver[F]] = {
    Mutex[F].map { mutex =>
      new IncrementalSolver(mutex)
    }
  }
}
