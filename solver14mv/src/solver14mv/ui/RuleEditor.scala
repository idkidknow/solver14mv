package solver14mv.ui

import com.raquo.laminar.api.L.*
import solver14mv.solver.Rule

object RuleEditor {

  trait Context {
    def rules: Signal[Set[Rule]]
  }

  type ModFunction = Context => Mod[HtmlElement]

  def apply(mods: ModFunction*): HtmlElement = {
    val rulesVar = Var(Set.empty[Rule])
    val ctx = new Context {
      override def rules: Signal[Set[Rule]] =
        rulesVar.signal
    }

    val elements = Rule.values.map { rule =>
      div(
        input(
          typ("checkbox"),
          onClick.mapToChecked --> rulesVar.updater[Boolean] { case (s, b) =>
            if (b) s + rule else s - rule
          },
        ),
        label(s"[${rule.productPrefix.head}]"),
      )
    }.toSeq

    div(
      elements,
      mods.map(_(ctx)),
    )
  }
}
