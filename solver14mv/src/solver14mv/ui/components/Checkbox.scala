package solver14mv.ui.components

import com.raquo.laminar.api.L.*
import japgolly.scalajs.react.*
import org.scalajs.dom
import solver14mv.ui.components.primitive.Checkbox as CheckboxPrimitive
import solver14mv.ui.components.react.PortalHub.PortalProps.MultipleRefSetter
import solver14mv.ui.components.react.PortalHub.globalPortalMultiple

import scala.scalajs.js

object Checkbox {
  final case class Context(checked: Var[Boolean])

  def checked(using ctx: Context): Var[Boolean] = ctx.checked

  type ModFunction = Context ?=> Mod[HtmlElement]

  @SuppressWarnings(Array("scalafix:DisableSyntax.defaultArgs"))
  def apply(default: Boolean = false)(mods: ModFunction*): Mod[HtmlElement] = {
    val checkedVar = Var(default)
    val ctx = Context(checkedVar)
    val onCheckedChange: js.Function2[Boolean, js.Object, Unit] = (c, _) => {
      checkedVar.set(c)
    }

    modSeq(
      globalPortalMultiple(
        2,
        checkedVar.signal.mapLazy { checked => setter =>
          Component(Props(checked, setter, onCheckedChange))
        },
      ),
      mods.map(_(using ctx)),
    )
  }

  private final case class Props(
      checked: Boolean,
      setter: MultipleRefSetter,
      onCheckedChange: js.Function2[Boolean, js.Object, Unit],
  )

  private val Component = ScalaFnComponent[Props] { props =>
    for {
      spanRef <- useRefToVdom[dom.html.Span]
      inputRef <- useRefToVdom[dom.html.Input]
      _ <- useLayoutEffect(for {
        _ <- spanRef.foreach(ref => props.setter.set(0, ref))
        _ <- inputRef.foreach(ref => props.setter.set(1, ref))
      } yield ())
    } yield CheckboxPrimitive.Root
      .withRef(spanRef)
      .apply(
        js.Dynamic.literal(
          `data-slot` = "checkbox",
          className =
            "border-input dark:bg-input/30 data-checked:bg-primary data-checked:text-primary-foreground dark:data-checked:bg-primary data-checked:border-primary aria-invalid:aria-checked:border-primary aria-invalid:border-destructive dark:aria-invalid:border-destructive/50 focus-visible:border-ring focus-visible:ring-ring/50 aria-invalid:ring-destructive/20 dark:aria-invalid:ring-destructive/40 flex size-4 items-center justify-center rounded-[4px] border transition-colors group-has-disabled/field:opacity-50 focus-visible:ring-3 aria-invalid:ring-3 peer relative shrink-0 outline-none after:absolute after:-inset-x-3 after:-inset-y-2 disabled:cursor-not-allowed disabled:opacity-50",
          checked = props.checked,
          onCheckedChange = props.onCheckedChange,
          inputRef = inputRef.raw,
        )
      )(
        CheckboxPrimitive.Indicator(
          js.Dynamic.literal(
            `data-slot` = "checkbox-indicator",
            className =
              "[&>svg]:size-3.5 grid place-content-center text-current transition-none",
          )
        )(CheckboxPrimitive.CheckIcon())
      )
  }
}
