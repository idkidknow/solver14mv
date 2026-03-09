package solver14mv.ui

import cats.effect.IO
import cats.effect.std.Dispatcher
import com.raquo.laminar.api.L.*
import solver14mv.solver
import solver14mv.solver.Clue
import solver14mv.solver.ConstraintSettings
import solver14mv.solver.SolveResult.CellSafety
import cats.syntax.all.*

object App {
  def apply(dispatcher: Dispatcher[IO]): HtmlElement = {
    val m = Var(8)
    val n = Var(8)
    val mnChanged = m.signal.combineWith(n.signal).changes
    val numToSet = Var(-2)
    val clueToSet = numToSet.signal.mapLazy {
      case -2 => Clue.QuestionMark
      case -1 => Clue.None
      case num => Clue.Number(num)
    }
    val clues: Var[Grid] = Var(Array.fill(m.now(), n.now())(Clue.None))
    val cellSafety: Var[Map[(Int, Int), CellSafety]] = Var(Map.empty)
    val constraints = Var(ConstraintSettings())

    val miniZincInitialized = Var(false)
    val initMiniZinc: IO[Unit] =
      solver.minizinc.raw.init[IO] *> IO.delay(miniZincInitialized.set(true))
    dispatcher.unsafeRunAndForget(initMiniZinc)

    val runningSolverCancel: Var[Option[IO[Unit]]] = Var(None)
    def stopSolver(): Unit = {
      runningSolverCancel
        .now()
        .foreach(cancel => dispatcher.unsafeRunAndForget(cancel))
      runningSolverCancel.set(None)
    }

    val cluesInput = modSeq(
      NumberInput(m, 1, 10),
      NumberInput(n, 1, 10),
      mnChanged --> clues.writer.contramap[(Int, Int)] { case (i, j) =>
        Array.fill(i, j)(Clue.None)
      },
      mnChanged --> { _ => stopSolver() },
      button(
        onClick.mapTo(
          Array.fill(m.now(), n.now())(Clue.None)
        ) --> clues.writer,
        onClick.mapTo(Map.empty) --> cellSafety.writer,
        onClick --> { _ => stopSolver() },
        "reset",
      ),
      NumberInput(numToSet, -2, 8),
      MinesweeperGrid(
        clues.signal,
        cellSafety.signal,
        _.onClick --> clues.updater[(Int, Int)] { case (grid, (i, j)) =>
          grid.updated(
            i,
            grid(i).updated(j, clueToSet.now()),
          )
        },
      ),
    )

    div(
      cluesInput,
      ConstraintEditor(
        _.constraints --> constraints.writer
      ),
      button(
        "solve",
        onClick --> { _ =>
          val solving: IO[Unit] = solver
            .solve[IO](clues.now(), constraints.now())
            .foreach { result =>
              IO.delay {
                cellSafety.update(
                  _.updated((result.i, result.j), result.safety)
                )
              }
            }
            .compile
            .drain
          val finish = IO.delay {
            runningSolverCancel.set(None)
          }
          val io = for {
            _ <- IO
              .delay(runningSolverCancel.now().getOrElse(().pure[IO]))
              .flatten
            fiber <- (solving *> finish).start
            _ <- IO.delay(runningSolverCancel.set(Some(fiber.cancel)))
          } yield ()
          dispatcher.unsafeRunAndForget(io)
        },
        disabled <-- miniZincInitialized.signal.not,
      ),
      button(
        "stop",
        onClick --> { _ => stopSolver() },
        disabled <-- runningSolverCancel.signal.map(_.isEmpty),
      ),
    )
  }
}
