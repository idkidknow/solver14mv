package solver14mv.ui.components.primitive

import japgolly.scalajs.react.Children
import japgolly.scalajs.react.component.JsForwardRef
import org.scalajs.dom

import scala.scalajs.js
import scala.scalajs.js.annotation.JSImport

object Slider {
  @js.native
  @JSImport("@base-ui/react", "Slider")
  object SliderRaw extends js.Object {
    val Root: js.Object = js.native
    val Control: js.Object = js.native
    val Track: js.Object = js.native
    val Indicator: js.Object = js.native
    val Thumb: js.Object = js.native
  }

  val Root =
    JsForwardRef[js.Object, Children.Varargs, dom.html.Div](SliderRaw.Root)

  val Control =
    JsForwardRef[js.Object, Children.Varargs, dom.html.Div](SliderRaw.Control)

  val Track =
    JsForwardRef[js.Object, Children.Varargs, dom.html.Div](SliderRaw.Track)

  val Indicator =
    JsForwardRef[js.Object, Children.None, dom.html.Div](SliderRaw.Indicator)

  val Thumb =
    JsForwardRef[js.Object, Children.None, dom.html.Div](SliderRaw.Thumb)
}
