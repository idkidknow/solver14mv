package solver14mv.solver.minizinc

import cats.effect.Async

import scala.annotation.nowarn
import scala.scalajs.js
import scala.scalajs.js.annotation.JSGlobal
import scala.scalajs.js.annotation.JSImport
import scala.scalajs.js.annotation.JSName

@SuppressWarnings(Array("scalafix:DisableSyntax.asInstanceOf"))
object raw {
  // (Vite)
  // JS:
  // import miniZincDataURL from "minizinc/minizinc.data?url";
  // import miniZincWasmURL from "minizinc/minizinc.wasm?url";
  // import miniZincWorkerURL from "minizinc/minizinc-worker.js?url";

  @js.native
  @JSImport("minizinc/minizinc.data?url", JSImport.Default)
  private def miniZincDataURL: String = js.native

  @js.native
  @JSImport("minizinc/minizinc.wasm?url", JSImport.Default)
  private def miniZincWasmURL: String = js.native

  @js.native
  @JSImport("minizinc/minizinc-worker.js?url", JSImport.Default)
  private def miniZincWorkerURL: String = js.native

  @js.native
  @JSImport("minizinc", "init")
  private def miniZincInit(config: js.Object): js.Promise[Unit] = js.native

  @js.native
  @JSGlobal("URL")
  class URL(@nowarn name: String, @nowarn base: js.Any) extends js.Object

  def init[F[_]: Async]: F[Unit] = {
    val config: js.Object = js.Dynamic.literal(
      workerURL = new URL(miniZincWorkerURL, js.`import`.meta.url),
      dataURL = new URL(miniZincDataURL, js.`import`.meta.url),
      wasmURL = new URL(miniZincWasmURL, js.`import`.meta.url),
    )
    Async[F].fromPromise(Async[F].delay(miniZincInit(config)))
  }

  opaque type ParamConfig <: js.Object = js.Object
  object ParamConfig {
    def apply(
        solver: Option[String],
        args: Map[String, js.Any],
    ): ParamConfig = {
      val dict =
        solver
          .map(solver => args.updated("solver", solver.asInstanceOf[js.Any]))
          .getOrElse(args)
      js.Dynamic.literal(dict.toSeq*)
    }
  }

  @js.native
  trait SolveConfig extends js.Object {
    val jsonOutput: Boolean = js.native
    val options: ParamConfig = js.native
  }

  object SolveConfig {
    def apply(jsonOutput: Boolean, options: ParamConfig): SolveConfig =
      js.Dynamic
        .literal(
          jsonOutput = jsonOutput,
          options = options,
        )
        .asInstanceOf[SolveConfig]
  }

  type SolveStatus = "ALL_SOLUTIONS" | "OPTIMAL_SOLUTION" | "UNSATISFIABLE" |
    "UNBOUNDED" | "UNSAT_OR_UNBOUNDED" | "SATISFIED" | "UNKNOWN" | "ERROR"

  @js.native
  trait SolveResult extends js.Object {
    val status: SolveStatus = js.native
    val solution: js.Object = js.native
  }

  @js.native
  @JSImport("minizinc", "Model")
  class Model extends js.Object {
    @JSName("clone")
    def cloneModel(): Model = js.native
    def addString(model: String): String = js.native
    def addDznString(dzn: String): String = js.native
    def addJson(data: js.Object): String = js.native
    def addFile(filename: String, contents: String, use: Boolean): Unit =
      js.native
    def solve(config: SolveConfig): js.Thenable[SolveResult] = js.native
  }
}
