package solver14mv.solver

import cats.Monoid
import cats.effect.Async
import cats.effect.Resource
import cats.effect.std.Mutex
import cats.syntax.all.*
import solver14mv.solver.z3.AST
import solver14mv.solver.z3.Context
import solver14mv.solver.z3.Solver
import solver14mv.solver.z3.Solver.CheckResult
import solver14mv.solver.z3.Sort

trait Solver14MV[F[_]] {
  def addClue(i: Int, j: Int, clue: Clue): F[Unit]
  def check: F[CheckResult]
  def checkSafety(i: Int, j: Int): F[Solver14MV.CellSafety]
  def getSafeCells: F[Seq[(Int, Int)]]
}

object Solver14MV {
  enum CellSafety {
    case Safe
    case Unsafe
    case Undetermined
  }

  def apply[F[_]: Async](
      m: Int,
      n: Int,
      constraints: Constraint*
  )(using ctx: Context): Resource[F, Solver14MV[F]] = Solver[F].evalMap {
    solver =>
      def addAssertions(as: Seq[AST]): F[Unit] =
        as.traverse(solver.assert(_)).void

      val isMineArray =
        AST.const("isMine", Sort.arrayN(Seq(Sort.int, Sort.int), Sort.bool))

      val decls = new Constraint.Declarations {
        override def isMineArr: AST = isMineArray
      }

      val constraint = Monoid.combineAll(constraints)

      val addClueAgnosticAssertions =
        addAssertions(constraint.generateAssertions(m, n, decls))

      for {
        _ <- addClueAgnosticAssertions
        mutex <- Mutex[F]
      } yield new Solver14MV[F] {
        override def addClue(i: Int, j: Int, clue: Clue): F[Unit] =
          mutex.lock.use { _ =>
            addAssertions(
              constraint.generateAssertionsFromClue(m, n, decls, i, j, clue)
            )
          }

        override def check: F[CheckResult] = mutex.lock.use { _ =>
          solver.check[F]
        }

        override def checkSafety(i: Int, j: Int): F[CellSafety] =
          mutex.lock.use { _ =>
            for {
              _ <- solver.push
              _ <- solver.assert(decls.isMine(i, j))
              ret <- solver.check
              _ <- solver.pop
            } yield ret match {
              case CheckResult.Sat => CellSafety.Unsafe
              case CheckResult.Unsat => CellSafety.Safe
              case CheckResult.Unknown => CellSafety.Undetermined
            }
          }

        override def getSafeCells: F[Seq[(Int, Int)]] = (0 until m)
          .flatMap(i => (0 until n).map(j => (i, j)))
          .to(LazyList)
          .traverseFilter { case (i, j) =>
            checkSafety(i, j).map {
              case CellSafety.Safe => Some((i, j))
              case _ => None
            }
          }
          .map(_.toSeq)
      }
  }
}
