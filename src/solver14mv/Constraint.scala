package solver14mv

enum Constraint {
  case Neighboring8Count(i: Int, j: Int, n: Int)
  case NotMine(i: Int, j: Int)
  case TotalMineCount(n: Int)
  case NoTriplets
}
