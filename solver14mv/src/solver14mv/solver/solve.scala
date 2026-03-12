package solver14mv.solver

import cats.effect.Async
import cats.syntax.all.*
import fs2.Stream
import solver14mv.solver.SolveResult.CellSafety
import solver14mv.solver.minizinc.MiniZincFiles
import solver14mv.solver.minizinc.raw.ParamConfig
import solver14mv.solver.minizinc.raw.SolveConfig

import scala.scalajs.js
import scala.scalajs.js.JSConverters.*

def solve[F[_]: Async](
    clues: Array[Array[Clue]],
    constraints: ConstraintSettings,
): Stream[F, SolveResult] = {
  val m = clues.length
  val n = clues.lift(0).map(_.length).getOrElse(0)
  val constraintsRaw = constraints.toRaw
  val cluesRaw = clues.map(_.map(_.code).toJSArray).toJSArray

  val modelBase = new minizinc.raw.Model()
  MiniZincFiles.files.foreach { case (name, content) =>
    modelBase.addFile(name, content, false)
  }
  val _ = modelBase.addString("""include "solver14mv.mzn";""")
  val _ = modelBase.addJson(
    js.Dynamic.literal(
      m = m,
      n = n,
      clues = cluesRaw,
    )
  )

  def checkCell(i: Int, j: Int, assertIsMine: Boolean): F[SolveResult] = {
    val model = modelBase.cloneModel()
    val _ = model.addJson(
      js.Dynamic.literal(
        constraints = constraintsRaw,
        // 1-indexed in .mzn, off-by-one
        assert_mine =
          js.Dynamic.literal(i = i + 1, j = j + 1, is_mine = assertIsMine),
      )
    )
    val thenable = Async[F].delay {
      model.solve(SolveConfig(true, ParamConfig(Some("chuffed"), Map())))
    }
    Async[F].fromThenable(thenable).map { ret =>
      ret.status match {
        case "UNSATISFIABLE" =>
          if (assertIsMine) SolveResult(i, j, SolveResult.CellSafety.Safe)
          else SolveResult(i, j, SolveResult.CellSafety.Mine)
        case _ => SolveResult(i, j, SolveResult.CellSafety.Indeterminate)
      }
    }
  }

  val indices: Stream[F, (Int, Int)] =
    Stream.range(0, m).flatMap(i => Stream.range(0, n).map(j => (i, j)))
  val check: Stream[F, SolveResult] = indices
    .parEvalMapUnordered(8) { case (i, j) => checkCell(i, j, true) } ++
    indices.parEvalMapUnordered(8) { case (i, j) => checkCell(i, j, false) }

  // Emit "Indeterminate" result only when both checks resulting in unsat (found "Indeterminate" twice)
  check
    .scan((Set.empty[(Int, Int)], Option.empty[SolveResult])) {
      case ((unsafe, _), curr) =>
        curr.safety match {
          case CellSafety.Indeterminate if !unsafe.contains((curr.i, curr.j)) =>
            (unsafe + ((curr.i, curr.j)), None)
          case _ => (unsafe, Some(curr))
        }
    }
    .map(_._2)
    .collect { case Some(r) => r }
}
