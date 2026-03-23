package solver14mv.ui.components

import com.raquo.laminar.api.L.*
import solver14mv.ui.components.primitive.Slider

import scala.scalajs.js

object Range {
  trait Context {
    def value: Var[Int]
  }

  def value(using ctx: Context): Var[Int] = ctx.value

  type ModFunction = Context ?=> Mod[HtmlElement]

  def apply(
      min: Int,
      max: Int,
      default: Int,
  )(mods: ModFunction*): HtmlElement = {
    val valueVar = Var(default)
    val ctx = new Context {
      override def value: Var[Int] = valueVar
    }
    val onValueChange: js.Function2[Int, js.Object, Unit] = (v, _) => {
      valueVar.set(v)
    }
    def render(value: Int) = {
      Slider.Root(
        js.Dynamic.literal(
          className = "data-horizontal:w-full data-vertical:h-full",
          `data-slot` = "slider",
          value = value,
          min = min,
          max = max,
          thumbAlignment = "edge",
          onValueChange = onValueChange,
        )
      )(
        Slider.Control(
          js.Dynamic.literal(
            className =
              "data-vertical:min-h-40 relative flex w-full touch-none items-center select-none data-disabled:opacity-50 data-vertical:h-full data-vertical:w-auto data-vertical:flex-col"
          )
        )(
          Slider.Track(
            js.Dynamic.literal(
              className =
                "bg-muted rounded-full data-horizontal:h-1 data-horizontal:w-full data-vertical:h-full data-vertical:w-1 relative grow overflow-hidden select-none",
              `data-slot` = "slider-track",
            )
          )(
            Slider.Indicator(
              js.Dynamic.literal(
                `data-slot` = "slider-range",
                className =
                  "bg-primary select-none data-horizontal:h-full data-vertical:w-full",
              )
            )
          ),
          Slider.Thumb(
            js.Dynamic.literal(
              `data-slot` = "slider-thumb",
              className =
                "border-ring ring-ring/50 relative size-3 rounded-full border bg-white transition-[color,box-shadow] after:absolute after:-inset-2 hover:ring-3 focus-visible:ring-3 focus-visible:outline-hidden active:ring-3 block shrink-0 select-none disabled:pointer-events-none disabled:opacity-50",
            )
          ),
        )
      )
    }

    react
      .wrap(render, valueVar.signal)
      .amend(
        mods.map(_(using ctx))
      )
  }
}
