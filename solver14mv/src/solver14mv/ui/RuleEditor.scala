package solver14mv.ui

import com.raquo.laminar.api.L.*
import solver14mv.solver.Rule
import solver14mv.ui.components.ToggleGroup

object RuleEditor {

  trait Context {
    def rules: Signal[Set[Rule]]
  }

  def rules(using ctx: Context): Signal[Set[Rule]] = ctx.rules

  type ModFunction = Context ?=> Mod[HtmlElement]

  def apply(mods: ModFunction*): Mod[HtmlElement] = {
    val rulesVar = Var(Set.empty[Rule])
    val ctx = new Context {
      override def rules: Signal[Set[Rule]] =
        rulesVar.signal
    }

    val items = Rule.values.map { rule => (_: ToggleGroup.Context) ?=>
      ToggleGroup.Item(rule.productPrefix)(rule.code)(rule.productPrefix)
    }.toSeq

    ToggleGroup(variant = "outline", size = "lg", multiple = true)(items*)(
      ToggleGroup.value.signal --> rulesVar.writer.contramap[List[String]] {
        strs =>
          strs.map(Rule.valueOf(_)).toSet
      },
      mods.map(_(using ctx)),
    )
  }
}
