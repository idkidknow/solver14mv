package solver14mv.solver.z3

opaque type Symbol = raw.Symbol

object Symbol {
  def int(i: Int)(using ctx: Context): Symbol =
    raw.z3.mk_int_symbol(ctx.getRaw, i)
  def str(s: String)(using ctx: Context): Symbol =
    raw.z3.mk_string_symbol(ctx.getRaw, s)

  extension (symbol: Symbol) {
    def getRaw: raw.Symbol = symbol
  }
}
