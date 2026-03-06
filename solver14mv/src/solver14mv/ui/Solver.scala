package solver14mv.ui

import cats.effect.IO
import cats.syntax.all.*
import solver14mv.solver.Clue
import solver14mv.solver.Constraint
import solver14mv.solver.Solver14MV
import solver14mv.solver.z3

object Solver {
  final case class Input(
      clues: Grid,
      constraint: Constraint,
  )

  def solve(
      input: Input,
      ctx: z3.Context,
  ): IO[Seq[(Int, Int)]] = {
    // TODO: diff the input and solve incrementally
    val clues = input.clues
    val m = clues.length
    val n = clues.lift(0).map(_.length).getOrElse(0)

    given z3.Context = ctx
    Solver14MV[IO](m, n, input.constraint).use { solver =>
      val clueSeq: Seq[(Int, Int, Clue)] = (for {
        i <- 0 until m
        j <- 0 until n
        clueOpt = clues(i)(j)
      } yield clueOpt.map(clue => (i, j, clue))).flatten
      val addClues: IO[Unit] = clueSeq.traverse { case (i, j, clue) =>
        solver.addClue(i, j, clue)
      }.void
      addClues *> solver.getSafeCells
    }
  }
}
