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

object App {
  def apply(dispatcher: Dispatcher[IO]): HtmlElement = {
    val m = Var(8)
    val n = Var(8)
    val mnChanged = m.signal.combineWith(n.signal).changes

    val clues: Var[Grid] = Var(Array.fill(m.now(), n.now())(Clue.None))
    val cellSafety: Var[Map[(Int, Int), CellSafety]] = Var(Map.empty)
    val rules = Var(Set.empty[Rule])
    val mineCount = Var(0)

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

    val basicInfoInput = div(
      label("m"),
      NumberInput(m, 1, 10),
      label("n"),
      NumberInput(n, 1, 10),
      mnChanged --> clues.writer.contramap[(Int, Int)] { case (i, j) =>
        Array.fill(i, j)(Clue.None)
      },
      mnChanged --> { _ => stopSolver() },
      br(),
      label("mine count"),
      NumberInput(mineCount, 0, 100),
    )

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
      BoardSettingsInput(),
      basicInfoInput,
      cluesInput,
      Button(variant = "secondary")(
        onClick.mapTo(
          Array.fill(m.now(), n.now())(Clue.None)
        ) --> clues.writer,
        onClick.mapTo(Map.empty) --> cellSafety.writer,
        onClick --> { _ => stopSolver() },
        "reset",
      ),
      RuleEditor(
        _.rules --> rules.writer
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
