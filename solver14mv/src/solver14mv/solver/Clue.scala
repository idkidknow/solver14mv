package solver14mv.solver

import cats.syntax.all.*

enum Clue {
  case None
  case QuestionMark
  case Vanilla(value: Int)
}

object Clue {
  extension (c: Clue) {
    def toDzn(i: Int, j: Int): Option[String] = c match {
      case None => Option.empty
      case QuestionMark => s"(i: $i, j: $j, ty: QuestionMark)".some
      case Vanilla(value) =>
        s"(i: $i, j: $j, ty: Vanilla, vanilla: $value)".some
    }
  }
}
