package solver14mv

import cats.effect.IO
import cats.effect.std.Dispatcher
import com.raquo.laminar.api.L.*
import org.scalajs.dom
import solver14mv.ui.App

import scala.scalajs.js.annotation.*

@JSExportTopLevel("renderApp")
def renderApp(root: dom.Element): Unit = {
  val io = Dispatcher
    .sequential[IO]
    .allocated
    .map(_._1) // leak it
    .flatMap { dispatcher =>
      IO.delay {
        render(root, App(dispatcher))
      }
    }
    .void
  io.unsafeRunAndForget()(using cats.effect.unsafe.IORuntime.global)
}
