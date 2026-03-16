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
            val content = clueSignal.map {
              case Clue.None => ("", "")
              case Clue.QuestionMark => ("?", "")
              case Clue.Flagged => ("🚩", "")
              case Clue.Vanilla(value) => (value.toString, "")
              case Clue.Multiple(value) => (value.toString, "M")
              case Clue.Liar(value) => (value.toString, "L")
              case Clue.Wall(value) =>
                val str = if (value.nonEmpty) value.mkString(" ") else "0"
                (str, "W")
              case Clue.Negation(value) => (value.toString, "N")
              case Clue.Cross(value) => (value.toString, "X")
              case Clue.Partition(value) => (value.toString, "P")
              case Clue.Eyesight(value) => (value.toString, "E")
              case Clue.MiniCross(value) => (value.toString, "X'")
              case Clue.Knight(value) => (value.toString, "K")
              case Clue.LongestWall(value) => (value.toString, "W'")
              case Clue.EyesightPrime(value) =>
                val str =
                  if (value > 0) s"↔$value"
                  else if (value < 0) s"↕${-value}"
                  else "0"
                (str, "E'")
            }
            td(
              button(
                minWidth("4em"),
                minHeight("4em"),
                onClick.mapTo((i, j)) --> onClickBus,
                backgroundColor <-- cellSafety
                  .map(_.get((i, j)))
                  .map {
                    case Some(CellSafety.Safe) => "#779977"
                    case Some(CellSafety.Mine) => "#AA4477"
                    case _ => ""
                  },
                div(
                  child.text <-- content.signal.map(_._1)
                ),
                div(
                  child.text <-- content.signal.map(_._2)
                ),
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
