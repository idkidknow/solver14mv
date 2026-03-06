package solver14mv.ui

import com.raquo.laminar.api.L.*
import solver14mv.solver.Clue

object MinesweeperGrid {
  trait Context {
    def onClick: EventStream[(Int, Int)]
  }

  type ModFunction = Context => Mod[HtmlElement]

  def apply(
      clues: Signal[Grid],
      safeCells: Signal[Set[(Int, Int)]],
      mods: ModFunction*
  ): HtmlElement = {
    val onClickBus = EventBus[(Int, Int)]()
    val ctx = new Context {
      override def onClick: EventStream[(Int, Int)] = onClickBus.stream
    }

    val rows =
      clues.signal.map(_.toSeq).splitByIndex { case (i, _, rowSignal) =>
        val items =
          rowSignal.map(_.toSeq).splitByIndex { case (j, _, clueSignal) =>
            val str = clueSignal.map {
              case Some(Clue.Number(n)) => n.toString
              case Some(Clue.QuestionMark) => "?"
              case None => ""
            }
            td(
              button(
                minWidth("4em"),
                minHeight("4em"),
                child.text <-- str,
                onClick.mapTo((i, j)) --> onClickBus,
                backgroundColor <-- safeCells
                  .map(_.contains(i, j))
                  .map(if (_) "#779977" else ""),
              )
            )
          }
        tbody(tr(children <-- items))
      }

    div(
      table(
        children <-- rows
      ),
      mods.map(_(ctx)),
    )
  }
}
