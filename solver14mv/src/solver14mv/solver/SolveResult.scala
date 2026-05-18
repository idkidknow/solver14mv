package solver14mv.solver

import cats.Eq
import cats.derived.*

enum SolveResult derives Eq {
  case Safe
  case Mine
  case Indeterminate
  case Unsat
}
