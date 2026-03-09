package solver14mv.solver

import cats.Monoid
import cats.syntax.all.*
import solver14mv.solver.z3.AST
import solver14mv.solver.z3.Context
import solver14mv.solver.z3.FuncDecl
import solver14mv.solver.z3.Sort
import solver14mv.solver.Constraint.Declarations

trait Constraint {
  def generateAssertions(m: Int, n: Int, decls: Declarations)(using
      ctx: Context
  ): Seq[AST]
  def generateAssertionsFromClue(
      m: Int,
      n: Int,
      decls: Declarations,
      i: Int,
      j: Int,
      clue: Clue,
  )(using ctx: Context): Seq[AST]
}

object Constraint {
  trait Declarations {
    def isMineArr: AST

    def isMine(i: Int, j: Int)(using ctx: Context): AST =
      AST.selectN(isMineArr, Seq(i, j))
  }

  trait ClueRelated extends Constraint {
    override def generateAssertions(m: Int, n: Int, decls: Declarations)(using
        ctx: Context
    ): Seq[AST] = Seq.empty
    override def generateAssertionsFromClue(
        m: Int,
        n: Int,
        decls: Declarations,
        i: Int,
        j: Int,
        clue: Clue,
    )(using ctx: Context): Seq[AST]
  }

  trait ClueAgnostic extends Constraint {
    override def generateAssertions(m: Int, n: Int, decls: Declarations)(using
        ctx: Context
    ): Seq[AST]
    override def generateAssertionsFromClue(
        m: Int,
        n: Int,
        decls: Declarations,
        i: Int,
        j: Int,
        clue: Clue,
    )(using ctx: Context): Seq[AST] = Seq.empty
  }

  def clueEqNeighboring8: Constraint = new Constraint.ClueRelated {
    override def generateAssertionsFromClue(
        m: Int,
        n: Int,
        decls: Declarations,
        i: Int,
        j: Int,
        clue: Clue,
    )(using ctx: Context): Seq[AST] = clue match {
      case Clue.QuestionMark => Seq.empty
      case Clue.Number(total) =>
        val nb = neighbors(m, n, i, j).map((k, l) => decls.isMine(k, l))
        Seq(AST.pbeq(nb, Seq.fill(nb.size)(1), total))
    }
  }

  def cluesAreNotMine: Constraint = new Constraint.ClueRelated {
    override def generateAssertionsFromClue(
        m: Int,
        n: Int,
        decls: Declarations,
        i: Int,
        j: Int,
        clue: Clue,
    )(using ctx: Context): Seq[AST] = Seq(AST.not(decls.isMine(i, j)))
  }

  def totalMineCountEq(cnt: Int): Constraint = new Constraint.ClueAgnostic {
    override def generateAssertions(m: Int, n: Int, decls: Declarations)(using
        ctx: Context
    ): Seq[AST] = {
      val all =
        (0 until m).flatMap(i => (0 until n).map(j => decls.isMine(i, j))).toSeq
      Seq(AST.pbeq(all, Seq.fill(all.size)(1), cnt))
    }
  }

  def noTriplets: Constraint = new Constraint.ClueAgnostic {
    override def generateAssertions(m: Int, n: Int, decls: Declarations)(using
        ctx: Context
    ): Seq[AST] = {
      import decls.isMine
      val horizontal = for {
        i <- 1 until m - 1
        j <- 0 until n
      } yield AST.pble(
        Seq(isMine(i - 1, j), isMine(i, j), isMine(i + 1, j)),
        Seq(1, 1, 1),
        2,
      )
      val vertical = for {
        i <- 0 until m
        j <- 1 until n - 1
      } yield AST.pble(
        Seq(isMine(i, j - 1), isMine(i, j), isMine(i, j + 1)),
        Seq(1, 1, 1),
        2,
      )
      val slash = for {
        i <- 1 until m - 1
        j <- 1 until n - 1
        back = AST.pble(
          Seq(isMine(i - 1, j - 1), isMine(i, j), isMine(i + 1, j + 1)),
          Seq(1, 1, 1),
          2,
        )
        forward = AST.pble(
          Seq(isMine(i - 1, j + 1), isMine(i, j), isMine(i + 1, j - 1)),
          Seq(1, 1, 1),
          2,
        )
        ret <- Seq(back, forward)
      } yield ret
      horizontal ++ vertical ++ slash
    }
  }

  def quadGe1: Constraint = new Constraint.ClueAgnostic {
    override def generateAssertions(m: Int, n: Int, decls: Declarations)(using
        ctx: Context
    ): Seq[AST] = {
      import decls.isMine
      for {
        i <- 0 until m - 1
        j <- 0 until n - 1
      } yield AST.pbge(
        Seq(
          isMine(i, j),
          isMine(i, j + 1),
          isMine(i + 1, j),
          isMine(i + 1, j + 1),
        ),
        Seq(1, 1, 1, 1),
        1,
      )
    }
  }

  def debug(c: Constraint) = new Constraint {
    override def generateAssertions(m: Int, n: Int, decls: Declarations)(using
        ctx: Context
    ): Seq[AST] = {
      val asts = c.generateAssertions(m, n, decls)
      asts.foreach(ast => println(ast.stringify))
      asts
    }
    override def generateAssertionsFromClue(
        m: Int,
        n: Int,
        decls: Declarations,
        i: Int,
        j: Int,
        clue: Clue,
    )(using ctx: Context): Seq[AST] = {
      val asts = c.generateAssertionsFromClue(m, n, decls, i, j, clue)
      asts.foreach(ast => println(ast.stringify))
      asts
    }
  }

  def empty: Constraint = new Constraint {
    override def generateAssertions(
        m: Int,
        n: Int,
        decls: Declarations,
    )(using
        ctx: Context
    ): Seq[AST] = Seq.empty
    override def generateAssertionsFromClue(
        m: Int,
        n: Int,
        decls: Declarations,
        i: Int,
        j: Int,
        clue: Clue,
    )(using ctx: Context): Seq[AST] = Seq.empty
  }

  private def neighbors(m: Int, n: Int, i: Int, j: Int): Seq[(Int, Int)] = for {
    di <- -1 to 1
    dj <- -1 to 1
    if di =!= 0 || dj =!= 0
    i1 = i + di
    j1 = j + dj
    if i1 >= 0 && i1 < m && j1 >= 0 && j1 < n
  } yield (i1, j1)

  /** not strict */
  given monoid: Monoid[Constraint] with {
    override def empty: Constraint = Constraint.empty
    override def combine(x: Constraint, y: Constraint): Constraint =
      combineAll(Seq(x, y))
    override def combineAll(as: IterableOnce[Constraint]): Constraint =
      new Constraint {
        override def generateAssertions(
            m: Int,
            n: Int,
            decls: Declarations,
        )(using ctx: Context): Seq[AST] = as.iterator.flatMap { a =>
          a.generateAssertions(m, n, decls)
        }.toSeq
        override def generateAssertionsFromClue(
            m: Int,
            n: Int,
            decls: Declarations,
            i: Int,
            j: Int,
            clue: Clue,
        )(using ctx: Context): Seq[AST] = as.iterator.flatMap { a =>
          a.generateAssertionsFromClue(m, n, decls, i, j, clue)
        }.toSeq
      }
  }
}
