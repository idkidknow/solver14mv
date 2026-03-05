package solver14mv

import solver14mv.z3.AST
import solver14mv.z3.Sort
import solver14mv.z3.Context
import solver14mv.z3.Solver
import solver14mv.z3.Z3Array

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

trait MineSweeperSolver {
  def addConstraint(c: Constraint): Unit
  def isSafe(i: Int, j: Int): Future[Boolean]
  def release(): Unit
}

object MineSweeperSolver {
  def apply(m: Int, n: Int)(using ctx: Context): MineSweeperSolver = {
    val solver = ctx.mkSolver
    val isMineArr = Z3Array.constN("isMine", Seq(Sort.int, Sort.int), Sort.bool)
    def isMine(i: Int, j: Int): AST = isMineArr.selectN(i, j)

    def neighbors(i: Int, j: Int): Seq[(Int, Int)] = for {
      di <- -1 to 1
      dj <- -1 to 1
      if di != 0 || dj != 0
      i1 = i + di
      j1 = j + dj
      if i1 >= 0 && i1 < m && j1 >= 0 && j1 < n
    } yield (i1, j1)
    
    new MineSweeperSolver {
      override def addConstraint(c: Constraint): Unit = c match {
        case Constraint.Neighboring8Count(i, j, n) =>
          val nb = neighbors(i, j).map((k, l) => isMine(k, l))
          solver.assert(AST.pbeq(nb, Seq.fill(nb.size)(1), n))
        case Constraint.NotMine(i, j) =>
          solver.assert(isMine(i, j).not)
        case Constraint.TotalMineCount(total) =>
          val all = (0 until m).flatMap(i => (0 until n).map(j => isMine(i, j))).toSeq
          solver.assert(AST.pbeq(all, Seq.fill(all.size)(1), total))
        case Constraint.NoTriplets =>
          for {
            i <- 1 until m - 1
            j <- 0 until n
          } {
            solver.assert(AST.pble(Seq(isMine(i - 1, j), isMine(i, j), isMine(i + 1, j)), Seq(1, 1, 1), 2))
          }
          for {
            i <- 0 until m
            j <- 1 until n - 1
          } {
            solver.assert(AST.pble(Seq(isMine(i, j - 1), isMine(i, j), isMine(i, j + 1)), Seq(1, 1, 1), 2))
          }
          for {
            i <- 1 until m - 1
            j <- 1 until n - 1
          } {
            solver.assert(AST.pble(Seq(isMine(i - 1, j - 1), isMine(i, j), isMine(i + 1, j + 1)), Seq(1, 1, 1), 2))
            solver.assert(AST.pble(Seq(isMine(i - 1, j + 1), isMine(i, j), isMine(i + 1, j - 1)), Seq(1, 1, 1), 2))
          }
      }

      override def isSafe(i: Int, j: Int): Future[Boolean] = {
        solver.push()
        solver.assert(isMine(i, j))
        solver.check.map { ret =>
          solver.pop()
          ret match {
            case Solver.CheckResult.Unsat => true
            case _ => false
          }
        }
      }

      override def release(): Unit = {
        solver.del()
      }
    }
  }
}
