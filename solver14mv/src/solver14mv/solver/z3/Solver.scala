package solver14mv.solver.z3

import cats.effect.Async
import cats.effect.Resource
import cats.effect.Sync
import cats.syntax.all.*

opaque type Solver = raw.Solver

object Solver {
  enum CheckResult {
    case Sat
    case Unsat
    case Unknown
  }

  def apply[F[_]: Sync](using ctx: Context): Resource[F, Solver] = {
    val acquire = Sync[F].delay {
      val s = raw.z3.mk_solver(ctx.getRaw)
      raw.z3.solver_inc_ref(ctx.getRaw, s)
      s
    }

    def release(s: Solver): F[Unit] =
      Sync[F].delay(raw.z3.solver_dec_ref(ctx.getRaw, s))

    Resource.make(acquire)(release)
  }

  extension (solver: Solver) {
    def getRaw: raw.Solver = solver

    def push[F[_]: Sync](using ctx: Context): F[Unit] = Sync[F].delay {
      raw.z3.solver_push(ctx.getRaw, solver)
    }

    def pop[F[_]: Sync](using ctx: Context): F[Unit] = Sync[F].delay {
      raw.z3.solver_pop(ctx.getRaw, solver, 1)
    }

    def assert[F[_]: Sync, A: ToAST](a: A)(using ctx: Context): F[Unit] =
      Sync[F].delay {
        raw.z3.solver_assert(ctx.getRaw, solver, ToAST[A].toAST(a).getRaw)
      }

    def check[F[_]: Async](using ctx: Context): F[CheckResult] = {
      val promise = Sync[F].delay {
        raw.z3.solver_check(ctx.getRaw, solver)
      }
      Async[F].fromPromise(promise).map {
        case -1 => CheckResult.Unsat
        case 1 => CheckResult.Sat
        case 0 => CheckResult.Unknown
      }
    }
  }
}
