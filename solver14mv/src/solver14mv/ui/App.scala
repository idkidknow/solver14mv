package solver14mv.ui

import cats.effect.IO
import cats.effect.std.Dispatcher
import cats.syntax.all.*
import com.raquo.laminar.api.L.*
import solver14mv.solver.Clue
import solver14mv.solver.Rule
import solver14mv.solver.SolveEvent
import solver14mv.solver.SolveEvent.CellSafety
import solver14mv.solver.Solver
import solver14mv.ui.BoardSettingsInput.Settings
import solver14mv.ui.components.Button
import solver14mv.utils.Grid

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
    val clues: Var[Grid[Clue]] = Var(
      Grid.fill(m.now(), n.now())(Clue.None)
    )
    val cellSafety: Var[Grid[CellSafety]] = Var(
      Grid.fill(m.now(), n.now())(CellSafety.Indeterminate)
    )
    val rules = Var(Set.empty[Rule])

    val solverVar = Var(Option.empty[Solver[IO]])
    val initSolver: Mod[Element] = {
      val io: IO[Unit] = for {
        _ <- solver14mv.solver.minizinc.raw.init[IO]
        solver <- Solver.incremental[IO]
        _ <- IO.delay(solverVar.set(Some(solver)))
      } yield ()
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
      clues.set(Grid.fill(m, n)(Clue.None))
      cellSafety.set(Grid.fill(m, n)(CellSafety.Indeterminate))
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
          grid.updated2D(i, j, clueBrush.now())
      }
    )

    val resetButton = Button(variant = "secondary")(
      onClick --> { _ => reset(m.now(), n.now()) },
      "reset",
    )

    val solveButton = Button()(
      "solve",
      onClick --> { _ =>
        val input = Solver.Input(clues.now(), rules.now(), mineCount.now())
        val solving: IO[Unit] = solverVar
          .now()
          .get
          .solve(input)
          .foreach {
            case SolveEvent.Begin(i, j) => IO.println(s"begin ($i, $j)")
            case SolveEvent.Pending(set) =>
              IO.println(s"pending: ${set.toString}")
            case SolveEvent.Result(i, j, safety) =>
              IO.delay {
                cellSafety.update { prev =>
                  prev.updated2D(i, j, safety)
                }
              } *> IO.println(s"result: ($i, $j) ${safety.toString}")
            case SolveEvent.Unsat => IO.println("unsat")
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
      disabled <-- solverVar.signal.mapLazy(_.isEmpty),
    )

    val stopButton = Button(variant = "secondary")(
      "stop",
      onClick --> { _ => stopSolver() },
      disabled <-- runningSolverCancel.signal.map(_.isEmpty),
    )

    div(
      cls <-- styles.root,
      initSolver,
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
