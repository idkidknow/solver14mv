package solver14mv.ui.components.primitive

import japgolly.scalajs.react.Children
import japgolly.scalajs.react.component.JsForwardRef
import org.scalajs.dom

import scala.scalajs.js
import scala.scalajs.js.annotation.JSImport

object Toggle {
  @js.native
  @JSImport("@base-ui/react/toggle", "Toggle")
  val ToggleRaw: js.Object = js.native

  val Toggle =
    JsForwardRef[js.Object, Children.Varargs, dom.html.Button](
      ToggleRaw
    )
}
