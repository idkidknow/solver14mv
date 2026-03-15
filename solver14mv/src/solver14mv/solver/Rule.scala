package solver14mv.solver

enum Rule {
  case Quad
  case Connected
  case Triplet
  case Outside
  case Dual
  case Snake
  case Balance
  case TripletPrime
}

object Rule {
  extension (r: Rule) {
    def toDzn: String = r.productPrefix

    def code: String = r match {
      case TripletPrime => "T'"
      case _ => r.productPrefix.take(1)
    }
  }
}
