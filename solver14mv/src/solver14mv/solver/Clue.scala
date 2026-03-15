package solver14mv.solver

enum Clue {
  case None
  case QuestionMark
  case Vanilla(value: Int)
  case Multiple(value: Int)
  case Liar(value: Int)
  case Wall(value: List[Int])
  case Negation(value: Int)
  case Cross(value: Int)
}
