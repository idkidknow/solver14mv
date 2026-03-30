package solver14mv.ui.components

import com.raquo.laminar.api.L.*
import com.raquo.laminar.nodes.ReactiveHtmlElement
import org.scalajs.dom

object Button {
  type Variant = "default" | "outline" | "ghost" | "destructive" | "secondary" |
    "link"
  type Size = "default" | "xs" | "sm" | "lg" | "icon" | "icon-xs" | "icon-sm" |
    "icon-lg"

  private def className(variant: Variant, size: Size): String = {
    val base =
      "group/button inline-flex shrink-0 items-center justify-center rounded-md border border-transparent bg-clip-padding text-xs/relaxed font-medium whitespace-nowrap transition-all outline-none select-none focus-visible:border-ring focus-visible:ring-2 focus-visible:ring-ring/30 active:translate-y-px disabled:pointer-events-none disabled:opacity-50 aria-invalid:border-destructive aria-invalid:ring-2 aria-invalid:ring-destructive/20 dark:aria-invalid:border-destructive/50 dark:aria-invalid:ring-destructive/40 [&_svg]:pointer-events-none [&_svg]:shrink-0 [&_svg:not([class*='size-'])]:size-4"
    val v = variant match {
      case "default" => "bg-primary text-primary-foreground hover:bg-primary/80"
      case "outline" =>
        "border-border hover:bg-input/50 hover:text-foreground aria-expanded:bg-muted aria-expanded:text-foreground dark:bg-input/30"
      case "secondary" =>
        "bg-secondary text-secondary-foreground hover:bg-secondary/80 aria-expanded:bg-secondary aria-expanded:text-secondary-foreground"
      case "ghost" =>
        "hover:bg-muted hover:text-foreground aria-expanded:bg-muted aria-expanded:text-foreground dark:hover:bg-muted/50"
      case "destructive" =>
        "bg-destructive/10 text-destructive hover:bg-destructive/20 focus-visible:border-destructive/40 focus-visible:ring-destructive/20 dark:bg-destructive/20 dark:hover:bg-destructive/30 dark:focus-visible:ring-destructive/40"
      case "link" =>
        "text-primary underline-offset-4 hover:underline"
    }
    val s = size match {
      case "default" =>
        "h-7 gap-1 px-2 text-xs/relaxed has-data-[icon=inline-end]:pr-1.5 has-data-[icon=inline-start]:pl-1.5 [&_svg:not([class*='size-'])]:size-3.5"
      case "xs" =>
        "h-5 gap-1 rounded-sm px-2 text-[0.625rem] has-data-[icon=inline-end]:pr-1.5 has-data-[icon=inline-start]:pl-1.5 [&_svg:not([class*='size-'])]:size-2.5"
      case "sm" =>
        "h-6 gap-1 px-2 text-xs/relaxed has-data-[icon=inline-end]:pr-1.5 has-data-[icon=inline-start]:pl-1.5 [&_svg:not([class*='size-'])]:size-3"
      case "lg" =>
        "h-8 gap-1 px-2.5 text-xs/relaxed has-data-[icon=inline-end]:pr-2 has-data-[icon=inline-start]:pl-2 [&_svg:not([class*='size-'])]:size-4"
      case "icon" => "size-7 [&_svg:not([class*='size-'])]:size-3.5"
      case "icon-xs" =>
        "size-5 rounded-sm [&_svg:not([class*='size-'])]:size-2.5"
      case "icon-sm" => "size-6 [&_svg:not([class*='size-'])]:size-3"
      case "icon-lg" => "size-8 [&_svg:not([class*='size-'])]:size-4"
    }
    cn(s"$base $v $s")
  }

  @SuppressWarnings(Array("scalafix:DisableSyntax.defaultArgs"))
  def apply(
      variant: Variant = "default",
      size: Size = "default",
  )(mods: Mod[ReactiveHtmlElement[dom.HTMLButtonElement]]*): HtmlElement = {
    button(
      cls(className(variant, size)),
      dataAttr("slot")("button"),
      dataAttr("variant")(variant),
      dataAttr("size")(size),
      mods,
    )
  }
}
