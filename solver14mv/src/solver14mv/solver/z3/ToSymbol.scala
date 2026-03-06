package solver14mv.solver.z3

trait ToSymbol[A] {
  def toSymbol(a: A): Symbol
}

object ToSymbol {
  def apply[A](using ToSymbol[A]): ToSymbol[A] = summon

  given identity: ToSymbol[Symbol] = new ToSymbol[Symbol] {
    override def toSymbol(a: Symbol): Symbol = a
  }

  given intToSymbol(using ctx: Context): ToSymbol[Int] = new ToSymbol[Int] {
    override def toSymbol(a: Int): Symbol = Symbol.int(a)
  }

  given stringToSymbol(using ctx: Context): ToSymbol[String] =
    new ToSymbol[String] {
      override def toSymbol(a: String): Symbol = Symbol.str(a)
    }
}
