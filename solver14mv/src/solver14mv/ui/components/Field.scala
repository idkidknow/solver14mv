package solver14mv.ui.components

import com.raquo.laminar.api.L.*
import com.raquo.laminar.nodes.ReactiveHtmlElement
import org.scalajs.dom

object Field {
  object Set {
    def apply(
        mods: Mod[ReactiveHtmlElement[dom.HTMLFieldSetElement]]*
    ): HtmlElement = {
      fieldSet(
        cls(
          cn(
            "gap-4 has-[>[data-slot=checkbox-group]]:gap-3 has-[>[data-slot=radio-group]]:gap-3 flex flex-col"
          )
        ),
        dataAttr("slot")("field-set"),
        mods,
      )
    }
  }

  object Legend {
    def apply(variant: "legend" | "label" = "legend")(
        mods: Mod[ReactiveHtmlElement[dom.HTMLLegendElement]]*
    ): HtmlElement = {
      legend(
        cls(
          cn(
            "mb-1.5 font-medium data-[variant=label]:text-sm data-[variant=legend]:text-base"
          )
        ),
        dataAttr("slot")("field-legend"),
        dataAttr("variant")(variant),
        mods,
      )
    }
  }

  object Group {
    def apply(
        mods: Mod[ReactiveHtmlElement[dom.HTMLDivElement]]*
    ): HtmlElement = {
      div(
        cls(
          cn(
            "gap-5 data-[slot=checkbox-group]:gap-3 *:data-[slot=field-group]:gap-4 group/field-group @container/field-group flex w-full flex-col"
          )
        ),
        dataAttr("slot")("field-group"),
        mods,
      )
    }
  }

  private def className(
      orientation: "vertical" | "horizontal" | "responsive"
  ): String = {
    val base =
      "data-[invalid=true]:text-destructive gap-2 group/field flex w-full"
    val o = orientation match {
      case "vertical" => "flex-col *:w-full [&>.sr-only]:w-auto"
      case "horizontal" =>
        "flex-row items-center has-[>[data-slot=field-content]]:items-start *:data-[slot=field-label]:flex-auto has-[>[data-slot=field-content]]:[&>[role=checkbox],[role=radio]]:mt-px"
      case "responsive" =>
        "flex-col *:w-full @md/field-group:flex-row @md/field-group:items-center @md/field-group:*:w-auto @md/field-group:has-[>[data-slot=field-content]]:items-start @md/field-group:*:data-[slot=field-label]:flex-auto [&>.sr-only]:w-auto @md/field-group:has-[>[data-slot=field-content]]:[&>[role=checkbox],[role=radio]]:mt-px"
    }
    cn(s"$base $o")
  }

  def apply(orientation: "vertical" | "horizontal" | "responsive" = "vertical")(
      mods: Mod[ReactiveHtmlElement[dom.HTMLDivElement]]*
  ): HtmlElement = {
    div(
      cls(className(orientation)),
      dataAttr("slot")("field"),
      mods,
    )
  }

  object Label {
    def apply(
        mods: Mod[ReactiveHtmlElement[dom.HTMLLabelElement]]*
    ): HtmlElement = {
      label(
        cls(
          cn(
            "has-data-checked:bg-primary/5 has-data-checked:border-primary/30 dark:has-data-checked:border-primary/20 dark:has-data-checked:bg-primary/10 gap-2 group-data-[disabled=true]/field:opacity-50 has-[>[data-slot=field]]:rounded-lg has-[>[data-slot=field]]:border *:data-[slot=field]:p-2.5 group/field-label peer/field-label flex w-fit leading-snug has-[>[data-slot=field]]:w-full has-[>[data-slot=field]]:flex-col"
          )
        ),
        dataAttr("slot")("field-label"),
        mods,
      )
    }
  }
}
