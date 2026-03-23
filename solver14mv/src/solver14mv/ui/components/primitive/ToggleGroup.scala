package solver14mv.ui.components.primitive

import japgolly.scalajs.react.Children
import japgolly.scalajs.react.component.JsForwardRef
import org.scalajs.dom

import scala.scalajs.js
import scala.scalajs.js.annotation.JSImport

object ToggleGroup {
  @js.native
  @JSImport("@base-ui/react/toggle-group", "ToggleGroup")
  val ToggleGroupRaw: js.Object = js.native

  val ToggleGroup =
    JsForwardRef[js.Object, Children.Varargs, dom.html.Div](
      ToggleGroupRaw
    )
}
