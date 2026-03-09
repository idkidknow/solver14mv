package solver14mv.solver

enum Clue {
  case Number(n: Int)
  case QuestionMark
  case None
}

object Clue {
  extension (c: Clue) {
    def code: Int = c match {
      case None => -1
      case QuestionMark => -2
      case Number(n) => n
    }
  }
}
