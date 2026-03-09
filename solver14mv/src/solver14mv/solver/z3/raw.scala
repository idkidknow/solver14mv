package solver14mv.solver.z3

import scala.scalajs.js

@SuppressWarnings(
  Array(
    "scalafix:DisableSyntax.null",
    "scalafix:DisableSyntax.var",
  )
)
object raw {
  opaque type Config <: js.Any = js.Any
  opaque type Context <: js.Any = js.Any
  opaque type AST <: js.Any = js.Any
  opaque type FuncDecl <: js.Any = js.Any
  opaque type Sort <: js.Any = js.Any
  opaque type Symbol <: js.Any = js.Any
  opaque type Solver <: js.Any = js.Any
  opaque type Pattern <: js.Any = js.Any
  type Z3LBool = -1 | 0 | 1

  @js.native
  trait Z3 extends js.Object {
    def mk_context(c: Config): Context = js.native
    def del_context(c: Context): Unit = js.native
    def mk_config(): Config = js.native
    def del_config(c: Config): Unit = js.native
    def mk_true(c: Context): AST = js.native
    def mk_false(c: Context): AST = js.native
    def mk_int_sort(c: Context): Sort = js.native
    def mk_bool_sort(c: Context): Sort = js.native
    def mk_int(c: Context, v: Int, ty: Sort): AST = js.native
    def mk_ite(c: Context, t1: AST, t2: AST, t3: AST): AST = js.native
    def ast_to_string(c: Context, a: AST): String = js.native
    def mk_int_symbol(c: Context, i: Int): Symbol = js.native
    def mk_string_symbol(c: Context, s: String): Symbol = js.native
    def mk_const(c: Context, s: Symbol, ty: Sort): AST = js.native
    def mk_solver(c: Context): Solver = js.native
    def solver_inc_ref(c: Context, s: Solver): Unit = js.native
    def solver_dec_ref(c: Context, s: Solver): Unit = js.native
    def mk_array_sort(c: Context, domain: Sort, range: Sort): Sort = js.native
    def mk_array_sort_n(c: Context, domain: js.Array[Sort], range: Sort): Sort =
      js.native
    def sort_to_string(c: Context, s: Sort): String = js.native
    def mk_select(c: Context, a: AST, i: AST): AST = js.native
    def mk_select_n(c: Context, a: AST, idxs: js.Array[AST]): AST = js.native
    def solver_push(c: Context, s: Solver): Unit = js.native
    def solver_pop(c: Context, s: Solver, n: Int): Unit = js.native
    def solver_assert(c: Context, s: Solver, a: AST): Unit = js.native
    def solver_check(c: Context, s: Solver): js.Promise[Z3LBool] = js.native
    def mk_pbeq(
        c: Context,
        args: js.Array[AST],
        coeffs: js.Array[Int],
        k: Int,
    ): AST = js.native
    def mk_pble(
        c: Context,
        args: js.Array[AST],
        coeffs: js.Array[Int],
        k: Int,
    ): AST = js.native
    def mk_pbge(
        c: Context,
        args: js.Array[AST],
        coeffs: js.Array[Int],
        k: Int,
    ): AST = js.native
    def mk_not(c: Context, a: AST): AST = js.native
    def mk_and(c: Context, args: js.Array[AST]): AST = js.native
    def mk_or(c: Context, args: js.Array[AST]): AST = js.native
    def mk_implies(c: Context, t1: AST, t2: AST): AST = js.native
    def mk_eq(c: Context, l: AST, r: AST): AST = js.native
    def mk_func_decl(
        c: Context,
        s: Symbol,
        domain: js.Array[Sort],
        range: Sort,
    ): FuncDecl = js.native
    def mk_app(c: Context, d: FuncDecl, args: js.Array[AST]): AST = js.native
    def mk_transitive_closure(c: Context, f: FuncDecl): FuncDecl = js.native
    def mk_bound(c: Context, index: Int, ty: Sort): AST = js.native
    def mk_forall(
        c: Context,
        weight: Int,
        patterns: js.Array[Pattern],
        sorts: js.Array[Sort],
        decl_names: js.Array[Symbol],
        body: AST,
    ): AST = js.native
    def mk_add(c: Context, args: js.Array[AST]): AST = js.native
    def mk_mul(c: Context, args: js.Array[AST]): AST = js.native
    def mk_le(c: Context, t1: AST, t2: AST): AST = js.native
    def mk_ge(c: Context, t1: AST, t2: AST): AST = js.native
    def mk_lt(c: Context, t1: AST, t2: AST): AST = js.native
    def mk_gt(c: Context, t1: AST, t2: AST): AST = js.native
  }

  private var inst: Z3 = null
  def z3: Z3 = inst
  def init(z3Inst: Z3): Unit = inst = z3Inst
}
