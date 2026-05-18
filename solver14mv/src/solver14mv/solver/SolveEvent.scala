package solver14mv.solver

import cats.Eq
import cats.derived.*

enum SolveEvent derives Eq {
  case Pending(set: Set[(Int, Int)])
  case Begin(i: Int, j: Int)
  case Result(i: Int, j: Int, safety: SolveEvent.CellSafety)
  case Unsat
}

object SolveEvent {
  enum CellSafety derives Eq {
    case Safe
    case Mine
    case Indeterminate
  }
}
