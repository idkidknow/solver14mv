package solver14mv.solver.z3

import scala.scalajs.js.JSConverters.*

opaque type AST = raw.AST

object AST {
  def const[S: ToSymbol](symbol: S, sort: Sort)(using ctx: Context): AST =
    raw.z3.mk_const(
      ctx.getRaw,
      ToSymbol[S].toSymbol(symbol).getRaw,
      sort.getRaw,
    )

  def int(n: Int)(using ctx: Context): AST =
    raw.z3.mk_int(ctx.getRaw, n, Sort.int.getRaw)

  def bool(b: Boolean)(using ctx: Context): AST =
    if (b) raw.z3.mk_true(ctx.getRaw) else raw.z3.mk_false(ctx.getRaw)

  def ite[A: ToAST, B: ToAST, C: ToAST](cond: A, t: B, f: C)(using
      ctx: Context
  ): AST =
    raw.z3.mk_ite(
      ctx.getRaw,
      ToAST[A].toAST(cond).getRaw,
      ToAST[B].toAST(t).getRaw,
      ToAST[C].toAST(f).getRaw,
    )

  def select[A: ToAST, B: ToAST](arr: A, idx: B)(using ctx: Context): AST =
    raw.z3.mk_select(
      ctx.getRaw,
      ToAST[A].toAST(arr).getRaw,
      ToAST[B].toAST(idx).getRaw,
    )

  def selectN[A: ToAST, B: ToAST](arr: A, idxs: Seq[B])(using
      ctx: Context
  ): AST =
    raw.z3.mk_select_n(
      ctx.getRaw,
      ToAST[A].toAST(arr).getRaw,
      idxs.map(ToAST[B].toAST(_).getRaw).toJSArray,
    )

  def pbeq[A: ToAST](args: Seq[A], coeffs: Seq[Int], k: Int)(using
      ctx: Context
  ): AST =
    raw.z3.mk_pbeq(
      ctx.getRaw,
      args.map(ToAST[A].toAST(_).getRaw).toJSArray,
      coeffs.toJSArray,
      k,
    )

  def pble[A: ToAST](args: Seq[A], coeffs: Seq[Int], k: Int)(using
      ctx: Context
  ): AST =
    raw.z3.mk_pble(
      ctx.getRaw,
      args.map(ToAST[A].toAST(_).getRaw).toJSArray,
      coeffs.toJSArray,
      k,
    )

  def pbge[A: ToAST](args: Seq[A], coeffs: Seq[Int], k: Int)(using
      ctx: Context
  ): AST =
    raw.z3.mk_pbge(
      ctx.getRaw,
      args.map(ToAST[A].toAST(_).getRaw).toJSArray,
      coeffs.toJSArray,
      k,
    )

  def not[A: ToAST](a: A)(using ctx: Context): AST =
    raw.z3.mk_not(ctx.getRaw, ToAST[A].toAST(a).getRaw)

  extension (ast: AST) {
    def getRaw: raw.AST = ast
    def stringify(using ctx: Context): String =
      raw.z3.ast_to_string(ctx.getRaw, ast)
  }
}
