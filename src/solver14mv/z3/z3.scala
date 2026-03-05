package solver14mv.z3

import scala.scalajs.js
import scala.scalajs.js.JSConverters.*
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

opaque type Config <: js.Any = js.Any
opaque type Context <: js.Any = js.Any
opaque type AST <: js.Any = js.Any
opaque type Sort <: js.Any = js.Any
opaque type Symbol <: js.Any = js.Any
opaque type Solver <: js.Any = js.Any
opaque type Z3LBool <: Int = Int

object Config {
  def apply(): Config = Z3().mk_config()
  extension (c: Config) {
    def del(): Unit = Z3().del_config(c)
    def mkContext: Context = Context(c)
  }
}

object Context {
  def apply(c: Config): Context = Z3().mk_context(c)
  extension (c: Context) {
    def del(): Unit = Z3().del_context(c)
    def mkSolver: Solver = Solver()(using c)
  }
}

object AST {
  def const[A: ToSymbol](s: A, ty: Sort)(using c: Context): AST = Z3().mk_const(c, ToSymbol[A].toSymbol(s), ty)

  def ite[A: ToAST, B: ToAST, C: ToAST](cond: A, t: B, f: C)(using c: Context): AST = {
    Z3().mk_ite(c, ToAST[A].toAST(cond), ToAST[B].toAST(t), ToAST[C].toAST(f))
  }

  def select[A: ToAST, B: ToAST](arr: A, idx: B)(using c: Context): AST = {
    Z3().mk_select(c, ToAST[A].toAST(arr), ToAST[B].toAST(idx))
  }

  def selectN[A: ToAST, B: ToAST](arr: A, idxs: Seq[B])(using c: Context): AST = {
    Z3().mk_select_n(c, ToAST[A].toAST(arr), idxs.map(idx => ToAST[B].toAST(idx)).toJSArray)
  }

  def pbeq[A: ToAST](args: Seq[A], coeffs: Seq[Int], k: Int)(using c: Context): AST = {
    Z3().mk_pbeq(c, args.map(arg => ToAST[A].toAST(arg)).toJSArray, coeffs.toJSArray, k)
  }

  def pble[A: ToAST](args: Seq[A], coeffs: Seq[Int], k: Int)(using c: Context): AST = {
    Z3().mk_pble(c, args.map(arg => ToAST[A].toAST(arg)).toJSArray, coeffs.toJSArray, k)
  }

  def pbge[A: ToAST](args: Seq[A], coeffs: Seq[Int], k: Int)(using c: Context): AST = {
    Z3().mk_pbge(c, args.map(arg => ToAST[A].toAST(arg)).toJSArray, coeffs.toJSArray, k)
  }

  extension (a: AST) {
    def stringify(using c: Context): String = Z3().ast_to_string(c, a)
    def not(using c: Context): AST = Z3().mk_not(c, a)
  }
}

object Solver {
  enum CheckResult {
    case Sat
    case Unsat
    case Unknown
  }

  def apply()(using c: Context): Solver = {
    val s = Z3().mk_solver(c)
    Z3().solver_inc_ref(c, s)
    s
  }
  extension (s: Solver) {
    def del()(using c: Context): Unit = Z3().solver_dec_ref(c, s)
    def push()(using c: Context): Unit = Z3().solver_push(c, s)
    def pop()(using c: Context): Unit = Z3().solver_pop(c, s, 1)
    def assert[A: ToAST](a: A)(using c: Context): Unit = Z3().solver_assert(c, s, ToAST[A].toAST(a))
    def check(using c: Context): Future[CheckResult] = Z3().solver_check(c, s).toFuture.map {
      case 1 => CheckResult.Sat
      case -1 => CheckResult.Unsat
      case _ => CheckResult.Unknown
    }
  }
}

object Sort {
  def int(using c: Context): Sort = Z3().mk_int_sort(c)
  def bool(using c: Context): Sort = Z3().mk_bool_sort(c)
  def array(domain: Sort, range: Sort)(using c: Context): Sort = Z3().mk_array_sort(c, domain, range)
  def arrayN(domain: Seq[Sort], range: Sort)(using c: Context): Sort = Z3().mk_array_sort_n(c, domain.toJSArray, range)

  extension (s: Sort) {
    def stringify(using c: Context): String = Z3().sort_to_string(c, s)
  }
}

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
  def mk_array_sort_n(c: Context, domain: js.Array[Sort], range: Sort): Sort = js.native
  def sort_to_string(c: Context, s: Sort): String = js.native
  def mk_select(c: Context, a: AST, i: AST): AST = js.native
  def mk_select_n(c: Context, a: AST, idxs: js.Array[AST]): AST = js.native
  def solver_push(c: Context, s: Solver): Unit = js.native
  def solver_pop(c: Context, s: Solver, n: Int): Unit = js.native
  def solver_assert(c: Context, s: Solver, a: AST): Unit = js.native
  def solver_check(c: Context, s: Solver): js.Promise[Z3LBool] = js.native
  def mk_pbeq(c: Context, args: js.Array[AST], coeffs: js.Array[Int], k: Int): AST = js.native
  def mk_pble(c: Context, args: js.Array[AST], coeffs: js.Array[Int], k: Int): AST = js.native
  def mk_pbge(c: Context, args: js.Array[AST], coeffs: js.Array[Int], k: Int): AST = js.native
  def mk_not(c: Context, a: AST): AST = js.native
}

object Z3 {
  private var z3: Z3 = null
  def init(z3: Z3): Unit = this.z3 = z3
  def apply(): Z3 = z3
}
