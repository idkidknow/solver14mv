package solver14mv.ui

import cats.effect.IO
import cats.effect.std.Dispatcher
import cats.syntax.all.*
import com.raquo.laminar.api.L.*
import solver14mv.solver
import solver14mv.solver.Clue
import solver14mv.solver.Rule
import solver14mv.solver.SolveResult.CellSafety
import solver14mv.ui.components.Button
import solver14mv.ui.BoardSettingsInput.Settings

object App {
  def apply(dispatcher: Dispatcher[IO]): HtmlElement = {
    val m = Var(8)
    val n = Var(8)
    val mineCount = Var(Option(26))
    val clues: Var[Grid] = Var(Array.fill(m.now(), n.now())(Clue.None))
    val cellSafety: Var[Map[(Int, Int), CellSafety]] = Var(Map.empty)
    val rules = Var(Set.empty[Rule])

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

    def reset(m: Int, n: Int): Unit = {
      stopSolver()
      clues.set(Array.fill(m, n)(Clue.None))
      cellSafety.set(Map.empty)
    }

    val clueBrush = Var(Clue.None)
    val cluesInput = div(
      ClueBrushEditor(
        _.clueBrush --> clueBrush
      ),
      MinesweeperGrid(
        clues.signal,
        cellSafety.signal,
        _.onClick --> clues.updater[(Int, Int)] { case (grid, (i, j)) =>
          grid.updated(
            i,
            grid(i).updated(j, clueBrush.now()),
          )
        },
      ),
    )
    div(
      Header(),
      BoardSettingsInput(
        BoardSettingsInput.settings --> Observer.combine(
          m.writer.contramap[Settings](_.row),
          n.writer.contramap[Settings](_.col),
          mineCount.writer.contramap[Settings](_.mineCount),
        )
      ),
      Signal.combine(m, n).changes.distinct --> { case (m, n) => reset(m, n) },
      RuleEditor(
        RuleEditor.rules --> rules.writer
      ),
      cluesInput,
      Button(variant = "secondary")(
        onClick --> { _ => reset(m.now(), n.now()) },
        "reset",
      ),
      Button()(
        "solve",
        onClick --> { _ =>
          val solving: IO[Unit] = solver
            .solve[IO](clues.now(), rules.now(), mineCount.now())
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
      Button(variant = "secondary")(
        "stop",
        onClick --> { _ => stopSolver() },
        disabled <-- runningSolverCancel.signal.map(_.isEmpty),
      ),
    )
  }
}
