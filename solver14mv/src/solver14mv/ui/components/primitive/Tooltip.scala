package solver14mv.ui.components.primitive

import japgolly.scalajs.react.Children
import japgolly.scalajs.react.component.JsFn
import japgolly.scalajs.react.component.JsForwardRef
import org.scalajs.dom

import scala.scalajs.js
import scala.scalajs.js.annotation.JSImport

object Tooltip {
  @js.native
  @JSImport("@base-ui/react/tooltip", "Tooltip")
  object TooltipRaw extends js.Object {
    val Provider: js.Object = js.native
    val Root: js.Object = js.native
    val Trigger: js.Object = js.native
    val Portal: js.Object = js.native
    val Positioner: js.Object = js.native
    val Popup: js.Object = js.native
    val Arrow: js.Object = js.native
  }

  @js.native
  trait ProviderProp extends js.Object {
    val delay: Int = js.native
  }

  val Provider = JsFn[js.Object, Children.Varargs](TooltipRaw.Provider)

  val Root = JsFn[js.Object, Children.Varargs](TooltipRaw.Root)

  val Trigger =
    JsForwardRef[js.Object, Children.Varargs, dom.Element](TooltipRaw.Trigger)

  val Portal =
    JsForwardRef[js.Object, Children.Varargs, dom.html.Div](TooltipRaw.Portal)

  val Positioner =
    JsForwardRef[js.Object, Children.Varargs, dom.html.Div](
      TooltipRaw.Positioner
    )

  val Popup =
    JsForwardRef[js.Object, Children.Varargs, dom.html.Div](TooltipRaw.Popup)

  val Arrow =
    JsForwardRef[js.Object, Children.None, dom.html.Div](TooltipRaw.Arrow)
}
