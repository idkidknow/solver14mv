package solver14mv.ui

import cats.Monoid
import cats.effect.IO
import cats.effect.std.Dispatcher
import com.raquo.laminar.api.L.*
import solver14mv.solver.Clue
import solver14mv.solver.Constraint
import solver14mv.solver.z3

object App {
  def apply(dispatcher: Dispatcher[IO], initZ3: IO[Unit]): HtmlElement = {
    val m = Var(8)
    val n = Var(8)
    val mnChanged = m.signal.combineWith(n.signal).changes
    val numToSet = Var(-2)
    val clueToSet = numToSet.signal.mapLazy {
      case -2 => Some(Clue.QuestionMark)
      case -1 => None
      case num => Some(Clue.Number(num))
    }
    val clues: Var[Grid] = Var(Array.fill(m.now(), n.now())(None))
    val safeCells: Var[Set[(Int, Int)]] = Var(Set.empty)
    val constraint = Var(Constraint.empty)

    val ctxVar: Var[Option[z3.Context]] = Var(None)
    // just leak it
    val getCtx: IO[Unit] =
      initZ3 *> z3.Context[IO].allocated.map(_._1).flatMap { ctx =>
        IO.delay { ctxVar.set(Some(ctx)) }
      }
    dispatcher.unsafeRunAndForget(getCtx)

    val cluesInput = modSeq(
      NumberInput(m, 1, 10),
      NumberInput(n, 1, 10),
      mnChanged --> clues.writer.contramap[(Int, Int)] { case (i, j) =>
        Array.fill(i, j)(None)
      },
      button(
        onClick.mapTo(
          Array.fill(m.now(), n.now())(Option.empty[Clue])
        ) --> clues.writer,
        onClick.mapTo(Set.empty) --> safeCells.writer,
        "reset",
      ),
      NumberInput(numToSet, -2, 8),
      MinesweeperGrid(
        clues.signal,
        safeCells.signal,
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
        _.constraints.map { c =>
          val constraints = Seq(
            if (c.clueEqNeighboring8) Some(Constraint.clueEqNeighboring8)
            else None,
            if (c.cluesAreNotMine) Some(Constraint.cluesAreNotMine) else None,
            c.totalMineCount.map(Constraint.totalMineCountEq(_)),
            if (c.noTriplets) Some(Constraint.noTriplets) else None,
          ).flatten
          Monoid.combineAll(constraints)
        } --> constraint.writer
      ),
      button(
        "solve",
        onClick --> { _ =>
          ctxVar.now().foreach { ctx =>
            val solverInput = Solver.Input(clues.now(), constraint.now())
            val io = Solver.solve(solverInput, ctx).map { result =>
              safeCells.set(result.toSet)
            }
            dispatcher.unsafeRunAndForget(io)
          }
        },
        disabled <-- ctxVar.signal.map(_.isEmpty),
      ),
    )
  }
}
