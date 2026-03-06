package solver14mv

import cats.effect.IO
import cats.effect.std.Dispatcher
import com.raquo.laminar.api.L.*
import org.scalajs.dom
import solver14mv.solver.z3
import solver14mv.ui.App

import scala.scalajs.js
import scala.scalajs.js.annotation.*

@JSExportTopLevel("renderApp")
def renderApp(
    root: dom.Element,
    getZ3: js.Function0[js.Promise[z3.raw.Z3]],
): Unit = {
  val io = Dispatcher
    .sequential[IO]
    .allocated
    .map(_._1) // leak it
    .flatMap { dispatcher =>
      val z3Inst = IO.fromPromise(IO.delay(getZ3()))
      val initZ3 = z3Inst.flatMap(z3Inst => IO.delay(z3.raw.init(z3Inst)))
      IO.delay {
        render(root, App(dispatcher, initZ3))
      }
    }
    .void
  io.unsafeRunAndForget()(using cats.effect.unsafe.IORuntime.global)
}
