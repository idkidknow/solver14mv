package solver14mv.z3

trait ToSymbol[A] {
  def toSymbol(a: A): Symbol
}

object ToSymbol {
  def apply[A](using ToSymbol[A]): ToSymbol[A] = summon

  extension [A: ToSymbol] (a: A) {
    def toSymbol: Symbol = ToSymbol[A].toSymbol(a)
  }

  given identity: ToSymbol[Symbol] = new ToSymbol[Symbol] {
    override def toSymbol(a: Symbol): Symbol = a
  }

  given intToSymbol(using ctx: Context): ToSymbol[Int] = new ToSymbol[Int] {
    override def toSymbol(a: Int): Symbol = Z3().mk_int_symbol(ctx, a)
  }

  given stringToSymbol(using ctx: Context): ToSymbol[String] = new ToSymbol[String] {
    override def toSymbol(a: String): Symbol = Z3().mk_string_symbol(ctx, a)
  }
}
