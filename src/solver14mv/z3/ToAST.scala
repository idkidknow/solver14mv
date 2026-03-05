package solver14mv.z3

trait ToAST[A] {
  def toAST(a: A): AST
}

object ToAST {
  def apply[A](using ToAST[A]): ToAST[A] = summon

  extension [A: ToAST] (a: A) {
    def toAST: AST = ToAST[A].toAST(a)
  }

  given identity: ToAST[AST] = new ToAST[AST] {
    override def toAST(a: AST): AST = a
  }

  given intToAST(using ctx: Context): ToAST[Int] = new ToAST[Int] {
    lazy val intSort = Z3().mk_int_sort(ctx)
    override def toAST(a: Int): AST = Z3().mk_int(ctx, a, intSort)
  }

  given boolToAST(using ctx: Context): ToAST[Boolean] = new ToAST[Boolean] {
    override def toAST(a: Boolean): AST = if (a) Z3().mk_true(ctx) else Z3().mk_false(ctx)
  }
}
