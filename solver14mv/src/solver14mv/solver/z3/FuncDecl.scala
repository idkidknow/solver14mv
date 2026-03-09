package solver14mv.solver.z3

import scala.scalajs.js.JSConverters.*

opaque type FuncDecl = raw.FuncDecl

object FuncDecl {
  def apply[A: ToSymbol](symbol: A, domain: Seq[Sort], range: Sort)(using
      ctx: Context
  ): FuncDecl =
    raw.z3.mk_func_decl(
      ctx.getRaw,
      ToSymbol[A].toSymbol(symbol).getRaw,
      domain.map(_.getRaw).toJSArray,
      range.getRaw,
    )

  extension (f: FuncDecl) {
    def getRaw: raw.FuncDecl = f

    def apply[A: ToAST](args: A*)(using Context): AST = AST.app(f, args*)

    def transitiveClosure(using ctx: Context): FuncDecl =
      raw.z3.mk_transitive_closure(ctx.getRaw, f)
  }
}
