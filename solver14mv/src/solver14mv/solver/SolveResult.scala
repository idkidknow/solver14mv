package solver14mv.solver

final case class SolveResult(
    i: Int,
    j: Int,
    safety: SolveResult.CellSafety,
)

object SolveResult {
  enum CellSafety {
    case Safe
    case Mine
    case Indeterminate
  }
}
