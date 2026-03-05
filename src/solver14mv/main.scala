package solver14mv

import scala.scalajs.js
import scala.scalajs.js.annotation.*
import scala.scalajs.js.JSConverters.*

import solver14mv.z3.Z3
import solver14mv.z3.Config
import solver14mv.z3.Context

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

object SolverAPI {
  @JSExportTopLevel("initZ3")
  def initZ3(z3: Z3): Unit = {
    Z3.init(z3)
  }

  @JSExportTopLevel("test")
  def test(
      m: Int,
      n: Int,
      total: Int,
      clues: js.Array[js.Object],
  ): js.Promise[String] = {
    val config = Config()
    val ctx = config.mkContext
    config.del()
    given Context = ctx
    val solver = MineSweeperSolver(m, n)

    val revealed = collection.mutable.Set.empty[(Int, Int)]

    clues.foreach { obj =>
      val clue = obj.asInstanceOf[js.Dynamic]
      val i = clue.i.asInstanceOf[Int]
      val j = clue.j.asInstanceOf[Int]
      if (clue.`type`.asInstanceOf[String] == "number") {
        val value = clue.value.asInstanceOf[Int]
        solver.addConstraint(Constraint.NotMine(i, j))
        solver.addConstraint(Constraint.Neighboring8Count(i, j, value))
      } else {
        solver.addConstraint(Constraint.NotMine(i, j))
      }
      revealed += ((i, j))
    }

    solver.addConstraint(Constraint.TotalMineCount(total))
    solver.addConstraint(Constraint.NoTriplets)

    val toCheck = for {
      i <- 0 until m
      j <- 0 until n
      if !revealed.contains((i, j))
    } yield { () =>
      solver.isSafe(i, j).map { b => (i, j, b) }
    }
    val result = toCheck.foldLeft(Future(List.empty[(Int, Int, Boolean)])) {
      case (acc, fut) =>
        acc.flatMap { list => fut().map { x => list :+ x } }
    }
    result
      .map { seq =>
        seq.filter(_._3).map { case (i, j, _) => s"($i, $j)" }.mkString(", ")
      }
      .andThen { _ =>
        solver.release()
        ctx.del()
      }
      .toJSPromise
  }
}
