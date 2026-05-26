package solver14mv.ui

import cats.syntax.all.*
import solver14mv.solver.SolveEvent
import solver14mv.solver.SolveEvent.CellSafety
import solver14mv.utils.Grid

final case class SolveState(
    safety: CellSafety,
    progress: SolveState.Progress,
)

object SolveState {
  enum Progress {
    case Outdated
    case Solving
    case Solved
    case NewlySolved
  }

  def update(
      states: Grid[SolveState],
      event: SolveEvent,
  ): Grid[SolveState] = {
    event match {
      case SolveEvent.Pending(set) =>
        val arr = states.toJs
        set.foreach { case (i, j) =>
          arr(i)(j) = arr(i)(j).copy(progress = Progress.Outdated)
        }
        Grid.unsafeFromJs(arr)

      case SolveEvent.Begin(i, j) =>
        states.updated2D(i, j, states(i, j).copy(progress = Progress.Solving))

      case SolveEvent.Result(i, j, safety) =>
        val newState =
          if (states(i, j).safety === safety)
            states(i, j).copy(progress = Progress.Solved)
          else SolveState(safety, Progress.NewlySolved)
        states.updated2D(i, j, newState)

      case SolveEvent.Unsat =>
        states.map(_.copy(progress = Progress.Outdated))
    }
  }

  def shiftToSolved(
      states: Grid[SolveState]
  ): Grid[SolveState] = {
    states.map {
      case SolveState(safety, Progress.NewlySolved) =>
        SolveState(safety, Progress.Solved)
      case state => state
    }
  }
}
