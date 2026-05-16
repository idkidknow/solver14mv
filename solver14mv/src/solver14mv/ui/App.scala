package solver14mv.ui

import cats.effect.IO
import cats.effect.std.Dispatcher
import cats.syntax.all.*
import com.raquo.laminar.api.L.*
import solver14mv.solver
import solver14mv.solver.Clue
import solver14mv.solver.Rule
import solver14mv.solver.SolveResult.CellSafety
import solver14mv.ui.BoardSettingsInput.Settings
import solver14mv.ui.components.Button

object App {
  trait Styles {
    val root: StrictSignal[String]
    val main: StrictSignal[String]
    val formDiv: StrictSignal[String]
    val gridDiv: StrictSignal[String]
    val boardSettingsInput: StrictSignal[String]
    val ruleEditor: StrictSignal[String]
    val clueBrushEditor: StrictSignal[String]
  }
  val styles = AppModuleCSS.as[Styles]

  def apply(dispatcher: Dispatcher[IO]): HtmlElement = {
    val m = Var(8)
    val n = Var(8)
    val mineCount = Var(Option(26))
    val clues: Var[Array[Array[Clue]]] = Var(
      Array.fill(m.now(), n.now())(Clue.None)
    )
    val cellSafety: Var[Array[Array[CellSafety]]] = Var(
      Array.fill(m.now(), n.now())(CellSafety.Indeterminate)
    )
    val rules = Var(Set.empty[Rule])

    val miniZincInitialized = Var(false)
    val initMiniZinc: Mod[Element] = {
      val io: IO[Unit] =
        solver.minizinc.raw.init[IO] *> IO.delay(miniZincInitialized.set(true))
      onMountCallback { _ => dispatcher.unsafeRunAndForget(io) }
    }

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
      cellSafety.set(Array.fill(m, n)(CellSafety.Indeterminate))
    }

    val header = Header()

    val boardSettingsInput = BoardSettingsInput(
      BoardSettingsInput.settings --> Observer.combine(
        m.writer.contramap[Settings](_.row),
        n.writer.contramap[Settings](_.col),
        mineCount.writer.contramap[Settings](_.mineCount),
      ),
      Signal.combine(m, n).changes.distinct --> { case (m, n) => reset(m, n) },
      cls <-- styles.boardSettingsInput,
    )

    val ruleEditor =
      RuleEditor(RuleEditor.rules --> rules.writer, cls <-- styles.ruleEditor)

    val clueBrush = Var(Clue.None)
    val addNumber = EventBus[Int]()
    val clueBrushEditor = ClueBrushEditor(
      ClueBrushEditor.clueBrush --> clueBrush,
      addNumber.stream --> ClueBrushEditor.addNumber,
      cls <-- styles.clueBrushEditor,
    )
    val brushEditShortcut = modSeq(
      onKeyDown.filter(_.code === "Minus").mapTo(-1) --> addNumber,
      onKeyDown.filter(_.code === "Equal").mapTo(1) --> addNumber,
    )

    val minesweeperGrid = MinesweeperGrid(clues.signal, cellSafety.signal)(
      MinesweeperGrid.onClick --> clues.updater[(Int, Int)] {
        case (grid, (i, j)) =>
          grid.updated(
            i,
            grid(i).updated(j, clueBrush.now()),
          )
      }
    )

    val resetButton = Button(variant = "secondary")(
      onClick --> { _ => reset(m.now(), n.now()) },
      "reset",
    )

    val solveButton = Button()(
      "solve",
      onClick --> { _ =>
        val solving: IO[Unit] = solver
          .solve[IO](clues.now(), rules.now(), mineCount.now())
          .foreach { result =>
            IO.delay {
              cellSafety.update { prev =>
                prev.updated(
                  result.i,
                  prev(result.i).updated(result.j, result.safety),
                )
              }
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
    )

    val stopButton = Button(variant = "secondary")(
      "stop",
      onClick --> { _ => stopSolver() },
      disabled <-- runningSolverCancel.signal.map(_.isEmpty),
    )

    div(
      cls <-- styles.root,
      initMiniZinc,
      header,
      div(
        cls <-- styles.main,
        div(
          cls <-- styles.formDiv,
          boardSettingsInput,
          ruleEditor,
          clueBrushEditor,
        ),
        brushEditShortcut,
        div(
          cls <-- styles.gridDiv,
          minesweeperGrid,
          div(
            resetButton,
            solveButton,
            stopButton,
          ),
        ),
      ),
    )
  }
}
