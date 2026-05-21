package solver14mv.solver

import cats.effect.Async
import cats.effect.Ref
import cats.syntax.all.*
import solver14mv.solver.minizinc.MiniZincFiles
import solver14mv.solver.minizinc.raw.Model as RawModel
import solver14mv.solver.minizinc.raw.ParamConfig
import solver14mv.solver.minizinc.raw.SolveConfig
import solver14mv.utils.Grid

import scala.scalajs.js.Dynamic.literal as lit

final class Model[F[_]: Async] private (
    cluesAndModel: Ref[F, (Grid[Clue], RawModel)],
    val rules: Set[Rule],
    val mineCount: Option[Int],
) {
  def solve(i: Int, j: Int): F[SolveResult] = {

    // Assert the cell is (not) mine and check unsat or not
    def checkCellUnsat(
        modelBase: RawModel,
        assertIsMine: Boolean,
    ): F[Boolean] = {
      val model = modelBase.cloneModel()
      val _ = model.addJson(
        lit(
          // 1-indexed in .mzn, off-by-one
          assert_mine = lit(i = i + 1, j = j + 1, is_mine = assertIsMine)
        )
      )
      def solveWith(solver: String): F[(ret: Boolean, unknown: Boolean)] =
        Async[F]
          .fromThenable(Async[F].delay {
            model.solve(SolveConfig(true, ParamConfig(Some(solver), Map())))
          })
          .map { ret =>
            ret.status match {
              case "UNSATISFIABLE" => true -> false
              case "UNKNOWN" => false -> true
              case _ => false -> false
            }
          }
          .attemptTap {
            case Left(_) =>
              Async[F].delay {
                org.scalajs.dom.console
                  .error(s"failed when checking ($i, $j) (assert is${
                      if (assertIsMine) " " else " not "
                    }mine)")
              }
            case Right(_) => ().pure
          }

      // fallback to highs if UNKNOWN
      for {
        (ret1, unknown) <- solveWith("chuffed")
        ret2 <- if (!unknown) ret1.pure else solveWith("highs").map(_.ret)
      } yield ret2
    }

    val result: F[SolveResult] = for {
      (_, model) <- cluesAndModel.get
      mustBeMine <- checkCellUnsat(model, false)
      mustBeSafe <- checkCellUnsat(model, true)
    } yield (mustBeMine, mustBeSafe) match {
      case (true, true) => SolveResult.Unsat
      case (true, false) => SolveResult.Mine
      case (false, true) => SolveResult.Safe
      case (false, false) => SolveResult.Indeterminate
    }

    def addRedundantConstraints(result: SolveResult): F[Unit] = {
      result match {
        case SolveResult.Mine =>
          cluesAndModel.update { case (clues, _) =>

            val newClues = clues.updated2D(i, j, Clue.Flagged)
            val newModel = Model.getRawModel(newClues, rules, mineCount)
            (newClues, newModel)
          }
        case SolveResult.Safe =>
          cluesAndModel.update { case (clues, _) =>
            val newClues = clues(i, j) match {
              case Clue.None =>
                clues.updated2D(i, j, Clue.QuestionMark)
              case _ => clues
            }
            val newModel = Model.getRawModel(newClues, rules, mineCount)
            (newClues, newModel)
          }
        case _ => ().pure
      }
    }

    result.flatTap(addRedundantConstraints)
  }
}

object Model {
  private def getRawModel(
      clues: Grid[Clue],
      rules: Set[Rule],
      mineCount: Option[Int],
  ): RawModel = {
    val (m, n) = clues.mn

    val cluesDzn = {
      val seq = for {
        i <- 0 until m
        j <- 0 until n
        // 1-indexed in .mzn, off-by-one
        ii = i + 1
        jj = j + 1
        dzn <- clues(i, j) match {
          case Clue.None => None
          case Clue.QuestionMark =>
            s"(i: $ii, j: $jj, ty: QuestionMark, data: 0)".some
          case Clue.Flagged =>
            s"(i: $ii, j: $jj, ty: Flagged, data: 0)".some
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
          case Clue.LongestWall(value) =>
            s"(i: $ii, j: $jj, ty: LongestWall, data: $value)".some
          case Clue.EyesightPrime(value) =>
            s"(i: $ii, j: $jj, ty: EyesightPrime, data: $value)".some
        }
      } yield dzn
      s"[${seq.mkString(",")}]"
    }
    val rulesDzn = {
      val set = rules.map(_.toDzn)
      s"{${set.mkString(",")}}"
    }

    val model = new RawModel()
    MiniZincFiles.files.foreach { case (name, content) =>
      model.addFile(name, content, false)
    }
    val _ = model.addString("""include "solver14mv.mzn";""")
    val _ = model.addJson(
      lit(
        m = m,
        n = n,
        mine_count = mineCount.getOrElse(-1),
      )
    )
    val _ = model.addDznString(s"clues = $cluesDzn;\nrules = $rulesDzn;")
    model
  }

  def apply[F[_]: Async](
      clues: Grid[Clue],
      rules: Set[Rule],
      mineCount: Option[Int],
  ): F[Model[F]] = {
    val model = getRawModel(clues, rules, mineCount)

    Ref.of((clues, model)).map { ref =>
      new Model(ref, rules, mineCount)
    }
  }
}
