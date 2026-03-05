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
  @JSExportTopLevel("helloWorld")
  def helloWorld(): Unit = {
    println("Hello world!")
  }

  @JSExportTopLevel("initZ3")
  def initZ3(z3: Z3): Unit = {
    Z3.init(z3)
  }
  @JSExportTopLevel("test")
  def test(): js.Promise[String] = {
    val config = Config()
    val ctx = config.mkContext
    config.del()
    given Context = ctx

    val m = 8
    val n = 8
    val matrix = Array.fill(m, n)(-1)
    val total = 26
    matrix(0)(7) = 3
    matrix(5)(7) = 4
    matrix(4)(5) = -2
    matrix(6)(5) = 4
    matrix(7)(5) = 2
    matrix(1)(5) = 5
    matrix(2)(6) = 3
    matrix(2)(7) = 2
    matrix(3)(4) = 4
    matrix(3)(6) = 3
    matrix(3)(7) = -2
    matrix(3)(5) = 4
    matrix(2)(3) = 3
    matrix(4)(2) = 2
    matrix(6)(4) = 4
    matrix(6)(6) = -2
    matrix(5)(2) = -2
    matrix(7)(2) = 2
    matrix(6)(2) = 2
    matrix(5)(1) = 1
    matrix(5)(3) = 4
    matrix(6)(1) = 2
    matrix(7)(1) = 1
    matrix(3)(2) = 3
    matrix(3)(3) = 4
    matrix(1)(3) = 3
    matrix(2)(1) = 5
    matrix(0)(2) = 2
    matrix(0)(3) = -2
    matrix(0)(4) = -2
    matrix(4)(0) = 2
    matrix(0)(0) = 2
    matrix(3)(0) = -2
    matrix(1)(1) = -2
    matrix(1)(4) = -2
    matrix(6)(0) = -2

    val solver = MineSweeperSolver(m, n)

    for {
      i <- 0 until m
      j <- 0 until n
    } {
      matrix(i)(j) match {
        case -1 =>
        case -2 => solver.addConstraint(Constraint.NotMine(i, j))
        case cnt =>
          solver.addConstraint(Constraint.NotMine(i, j))
          solver.addConstraint(Constraint.Neighboring8Count(i, j, cnt))
      }
    }
    solver.addConstraint(Constraint.TotalMineCount(total))
    solver.addConstraint(Constraint.NoTriplets)

    val toCheck = for {
      i <- 0 until m
      j <- 0 until n
      if matrix(i)(j) == -1
    } yield {
      () => solver.isSafe(i, j).map { b => (i, j, b) }
    }
    val result = toCheck.foldLeft(Future(List.empty[(Int, Int, Boolean)])) { case (acc, fut) =>
      acc.flatMap { list => fut().map { x => list :+ x } }
    }
    result.map { seq =>
      seq.filter(_._3).map { case (i, j, _) => s"($i, $j)" }.mkString(", ")
    }.toJSPromise
  }
}
