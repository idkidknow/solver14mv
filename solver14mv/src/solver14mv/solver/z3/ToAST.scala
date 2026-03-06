package solver14mv.solver.z3

trait ToAST[A] {
  def toAST(a: A): AST
}

object ToAST {
  def apply[A](using ToAST[A]): ToAST[A] = summon

  given identity: ToAST[AST] = new ToAST[AST] {
    override def toAST(a: AST): AST = a
  }

  given intToAST(using ctx: Context): ToAST[Int] with {
    override def toAST(a: Int): AST = AST.int(a)
  }

  given booleanToAST(using ctx: Context): ToAST[Boolean] with {
    override def toAST(a: Boolean): AST = AST.bool(a)
  }
}
