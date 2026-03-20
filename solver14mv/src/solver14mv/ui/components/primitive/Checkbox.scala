package solver14mv.ui.components.primitive

import japgolly.scalajs.react.Children
import japgolly.scalajs.react.component.JsForwardRef
import org.scalajs.dom

import scala.scalajs.js
import scala.scalajs.js.annotation.JSImport

object Checkbox {
  @js.native
  @JSImport("@base-ui/react", "Checkbox")
  object CheckboxRaw extends js.Object {
    val Root: js.Object = js.native
    val Indicator: js.Object = js.native
  }

  @js.native
  @JSImport("lucide-react", "CheckIcon")
  val CheckIconRaw: js.Object = js.native

  val Root =
    JsForwardRef[js.Object, Children.Varargs, dom.html.Element](
      CheckboxRaw.Root
    )

  val Indicator =
    JsForwardRef[js.Object, Children.Varargs, dom.html.Span](
      CheckboxRaw.Indicator
    )

  val CheckIcon =
    JsForwardRef[Null, Children.None, dom.SVGSVGElement](
      CheckIconRaw
    )
}
