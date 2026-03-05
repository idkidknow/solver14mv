package solver14mv.z3

opaque type Z3Array = AST

object Z3Array {
  def sort(domain: Sort, range: Sort)(using ctx: Context): Sort =
    Sort.array(domain, range)
  def sortN(domain: Seq[Sort], range: Sort)(using ctx: Context): Sort =
    Sort.arrayN(domain, range)
  def const[A: ToSymbol](s: A, domain: Sort, range: Sort)(using
      ctx: Context
  ): Z3Array = AST.const(s, sort(domain, range))
  def constN[A: ToSymbol](s: A, domain: Seq[Sort], range: Sort)(using
      ctx: Context
  ): Z3Array = AST.const(s, sortN(domain, range))

  extension (a: Z3Array) {
    def apply[A: ToAST](i: A)(using ctx: Context): AST = AST.select(a, i)
    def selectN[A: ToAST](idxs: A*)(using ctx: Context): AST = AST.selectN(a, idxs)
  }

  given toAST: ToAST[Z3Array] = new ToAST[Z3Array] {
    override def toAST(a: Z3Array): AST = a
  }
}
