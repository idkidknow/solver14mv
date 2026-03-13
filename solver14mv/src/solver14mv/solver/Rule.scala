package solver14mv.solver

enum Rule {
  case Quad
  case Connected
  case Triplet
  case Outside
  case Dual
  case Snake
  case Balance
}

object Rule {
  extension (r: Rule) {
    def toDzn: String = r.productPrefix
  }
}
