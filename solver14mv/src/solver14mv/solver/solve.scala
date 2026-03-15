package solver14mv.solver

import cats.effect.Async
import cats.syntax.all.*
import fs2.Stream
import solver14mv.solver.SolveResult.CellSafety
import solver14mv.solver.minizinc.MiniZincFiles
import solver14mv.solver.minizinc.raw.ParamConfig
import solver14mv.solver.minizinc.raw.SolveConfig

import scala.scalajs.js
import scala.scalajs.js.Dynamic.literal as lit

def solve[F[_]: Async](
    clues: Array[Array[Clue]],
    rules: Set[Rule],
    mineCount: Int,
): Stream[F, SolveResult] = {
  val m = clues.length
  val n = clues.lift(0).map(_.length).getOrElse(0)
  val cluesDzn = {
    val seq = for {
      i <- 0 until m
      j <- 0 until n
      // 1-indexed in .mzn, off-by-one
      ii = i + 1
      jj = j + 1
      dzn <- clues(i)(j) match {
        case Clue.None => None
        case Clue.QuestionMark =>
          s"(i: $ii, j: $jj, ty: QuestionMark, data: 0)".some
        case Clue.Vanilla(value) =>
          s"(i: $ii, j: $jj, ty: Vanilla, data: $value)".some
        case Clue.Multiple(value) =>
          s"(i: $ii, j: $jj, ty: Multiple, data: $value)".some
        case Clue.Liar(value) =>
          s"(i: $ii, j: $jj, ty: Liar, data: $value)".some
        case Clue.Wall(value) =>
          val data = if (value.nonEmpty) value.mkString else "0"
          s"(i: $ii, j: $jj, ty: Wall, data: $data)".some
        case Clue.Negation(value) =>
          s"(i: $ii, j: $jj, ty: Negation, data: $value)".some
        case Clue.Cross(value) =>
          s"(i: $ii, j: $jj, ty: Cross, data: $value)".some
        case Clue.Partition(value) =>
          s"(i: $ii, j: $jj, ty: Partition, data: $value)".some
        case Clue.Eyesight(value) =>
          s"(i: $ii, j: $jj, ty: Eyesight, data: $value)".some
        case Clue.MiniCross(value) =>
          s"(i: $ii, j: $jj, ty: MiniCross, data: $value)".some
        case Clue.Knight(value) =>
          s"(i: $ii, j: $jj, ty: Knight, data: $value)".some
      }
    } yield dzn
    s"[${seq.mkString(",")}]"
  }
  val rulesDzn = {
    val set = rules.map(_.toDzn)
    s"{${set.mkString(",")}}"
  }

  val modelBase = new minizinc.raw.Model()
  MiniZincFiles.files.foreach { case (name, content) =>
    modelBase.addFile(name, content, false)
  }
  val _ = modelBase.addString("""include "solver14mv.mzn";""")
  val _ = modelBase.addJson(
    lit(
      m = m,
      n = n,
      mine_count = mineCount,
    )
  )
  val _ = modelBase.addDznString(s"clues = $cluesDzn;\nrules = $rulesDzn;")

  def checkCell(i: Int, j: Int, assertIsMine: Boolean): F[SolveResult] = {
    val model = modelBase.cloneModel()
    val _ = model.addJson(
      lit(
        // 1-indexed in .mzn, off-by-one
        assert_mine = lit(i = i + 1, j = j + 1, is_mine = assertIsMine)
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
