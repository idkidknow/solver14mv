package solver14mv.ui

import com.raquo.laminar.api.L.*
import solver14mv.solver.Clue
import solver14mv.solver.SolveResult.CellSafety

object MinesweeperGrid {
  trait Context {
    def onClick: EventStream[(Int, Int)]
  }

  type ModFunction = Context => Mod[HtmlElement]

  def apply(
      clues: Signal[Grid],
      cellSafety: Signal[Map[(Int, Int), CellSafety]],
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
              case Clue.Number(n) => n.toString
              case Clue.QuestionMark => "?"
              case Clue.None => ""
            }
            td(
              button(
                minWidth("4em"),
                minHeight("4em"),
                child.text <-- str,
                onClick.mapTo((i, j)) --> onClickBus,
                backgroundColor <-- cellSafety
                  .map(_.get((i, j)))
                  .map {
                    case Some(CellSafety.Safe) => "#779977"
                    case Some(CellSafety.Mine) => "#AA4477"
                    case _ => ""
                  },
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
