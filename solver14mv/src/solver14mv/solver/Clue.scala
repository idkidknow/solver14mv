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
  case Partition(value: Int)
  case Eyesight(value: Int)
  case MiniCross(value: Int)
  case Knight(value: Int)
  case LongestWall(value: Int)
}
