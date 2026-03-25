package solver14mv.ui.components

import com.raquo.laminar.api.L.*
import japgolly.scalajs.react.*
import japgolly.scalajs.react.vdom.html_<^.*
import solver14mv.ui.components.primitive.Toggle.Toggle as TogglePrimitive

import scala.scalajs.js

class ToggleGroupItem private (val inner: VdomNode)

object ToggleGroupItem {
  private def className(
      variant: "default" | "outline",
      size: "default" | "sm" | "lg",
  ): String = {
    val base1 =
      "group-data-[spacing=0]/toggle-group:rounded-none group-data-[spacing=0]/toggle-group:px-2 group-data-horizontal/toggle-group:data-[spacing=0]:first:rounded-l-lg group-data-vertical/toggle-group:data-[spacing=0]:first:rounded-t-lg group-data-horizontal/toggle-group:data-[spacing=0]:last:rounded-r-lg group-data-vertical/toggle-group:data-[spacing=0]:last:rounded-b-lg shrink-0 focus:z-10 focus-visible:z-10 group-data-horizontal/toggle-group:data-[spacing=0]:data-[variant=outline]:border-l-0 group-data-vertical/toggle-group:data-[spacing=0]:data-[variant=outline]:border-t-0 group-data-horizontal/toggle-group:data-[spacing=0]:data-[variant=outline]:first:border-l group-data-vertical/toggle-group:data-[spacing=0]:data-[variant=outline]:first:border-t"
    val base2 =
      "hover:text-foreground aria-pressed:bg-muted focus-visible:border-ring focus-visible:ring-ring/50 aria-invalid:ring-destructive/20 dark:aria-invalid:ring-destructive/40 aria-invalid:border-destructive data-[state=on]:bg-muted gap-1 rounded-lg text-sm font-medium transition-all [&_svg:not([class*='size-'])]:size-4 group/toggle inline-flex items-center justify-center whitespace-nowrap outline-none hover:bg-muted focus-visible:ring-[3px] disabled:pointer-events-none disabled:opacity-50 [&_svg]:pointer-events-none [&_svg]:shrink-0"
    val v = variant match {
      case "default" => "bg-transparent"
      case "outline" => "border-input hover:bg-muted border bg-transparent"
    }
    val s = size match {
      case "default" => "h-8 min-w-8 px-2"
      case "sm" =>
        "h-7 min-w-7 rounded-[min(var(--radius-md),12px)] px-1.5 text-[0.8rem]"
      case "lg" => "h-9 min-w-9 px-2.5"
    }
    cn(s"$base1 $base2 $v $s")
  }

  def apply(
      value: String
  )(
      children: Element*
  )(using groupCtx: ToggleGroup.Context): ToggleGroupItem = {
    val childrenReact: Seq[VdomNode] =
      children.map(child => react.Laminar[Unit](_ => child)(()))

    val component = TogglePrimitive.withKey(value)(
      js.Dynamic.literal(
        `data-slot` = "toggle-group-item",
        `data-variant` = groupCtx.variant,
        `data-size` = groupCtx.size,
        `data-spacing` = groupCtx.spacing,
        className = className(groupCtx.variant, groupCtx.size),
        value = value,
      )
    )(childrenReact*)
    new ToggleGroupItem(component)
  }
}
