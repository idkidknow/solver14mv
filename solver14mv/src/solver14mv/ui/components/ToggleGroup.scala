package solver14mv.ui.components

import com.raquo.laminar.api.L.*
import japgolly.scalajs.react.*
import japgolly.scalajs.react.vdom.Implicits.*
import org.scalajs.dom
import solver14mv.ui.components.primitive.ToggleGroup.ToggleGroup as ToggleGroupPrimitive
import solver14mv.ui.components.react.PortalHub.globalDest

import scala.scalajs.js
import scala.scalajs.js.JSConverters.*

object ToggleGroup {
  final case class Context(
      variant: "default" | "outline",
      size: "default" | "sm" | "lg",
      spacing: Int,
      orientation: "horizontal" | "vertical",
      value: Var[List[String]],
  )

  def value(using ctx: Context): Var[List[String]] = ctx.value

  type ItemFunction = Context ?=> ToggleGroupItem
  type ModFunction = Context ?=> Mod[HtmlElement]

  def apply(
      variant: "default" | "outline" = "default",
      size: "default" | "sm" | "lg" = "default",
      spacing: Int = 0,
      orientation: "horizontal" | "vertical" = "horizontal",
      init: List[String] = List.empty,
      multiple: Boolean = false,
  )(items: ItemFunction*)(mods: ModFunction*): Mod[HtmlElement] = {
    val valueVar = Var(init)
    val ctx =
      Context(variant, size, spacing, orientation, valueVar)
    val onValueChange: js.Function2[js.Array[String], js.Object, Unit] =
      (value, _) => {
        valueVar.set(value.toList)
      }
    val itemArr = items.map(_(using ctx).inner).toVdomArray
    def render(value: List[String], ref: Ref.ToVdom[dom.Element]) = {
      ToggleGroupPrimitive
        .withRef(ref)
        .apply(
          js.Dynamic.literal(
            `data-slot` = "toggle-group",
            `data-variant` = variant,
            `data-spacing` = spacing,
            `data-orientation` = orientation,
            style = js.Dynamic.literal(`--gap` = spacing),
            className = cn(
              "rounded-lg data-[size=sm]:rounded-[min(var(--radius-md),10px)] group/toggle-group flex w-fit flex-row items-center gap-[--spacing(var(--gap))] data-vertical:flex-col data-vertical:items-stretch"
            ),
            multiple = multiple,
            value = value.toJSArray,
            onValueChange = onValueChange,
          )
        )(itemArr)
    }

    modSeq(
      globalDest(valueVar.signal.mapLazy { value => ref =>
        render(value, ref)
      }),
      mods.map(_(using ctx)),
    )
  }
}
