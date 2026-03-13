package solver14mv.ui

import com.raquo.laminar.api.L.*
import solver14mv.solver.Clue

object ClueBrushEditor {

  trait Context {
    def clueBrush: Signal[Clue]
  }

  type ModFunction = Context => Mod[HtmlElement]

  def apply(mods: ModFunction*): HtmlElement = {
    val clueBrushVar = Var(Clue.None)
    val ctx = new Context {
      override def clueBrush: Signal[Clue] =
        clueBrushVar.signal
    }

    val selected = Var("None")
    def selectOption(v: String) = option(value(v), v)
    val options = Seq("None", "?", "Vanilla", "Multiple", "Liar", "Negation")

    val dataInput = selected.signal.splitOne(identity) {
      case ("None", _, signal) =>
        div(signal.mapTo(Clue.None) --> clueBrushVar.writer)
      case ("?", _, signal) =>
        div(signal.mapTo(Clue.QuestionMark) --> clueBrushVar.writer)
      case ("Vanilla", _, signal) =>
        val writer =
          clueBrushVar.writer.contracollect[String](Function.unlift { s =>
            s.toIntOption.map(Clue.Vanilla(_))
          })
        input(
          typ("text"),
          onInput.mapToValue --> writer,
          inContext { node =>
            signal.mapTo(node.ref.value) --> writer
          },
        )
      case ("Multiple", _, signal) =>
        val writer =
          clueBrushVar.writer.contracollect[String](Function.unlift { s =>
            s.toIntOption.map(Clue.Multiple(_))
          })
        input(
          typ("text"),
          onInput.mapToValue --> writer,
          inContext { node =>
            signal.mapTo(node.ref.value) --> writer
          },
        )
      case ("Liar", _, signal) =>
        val writer =
          clueBrushVar.writer.contracollect[String](Function.unlift { s =>
            s.toIntOption.map(Clue.Liar(_))
          })
        input(
          typ("text"),
          onInput.mapToValue --> writer,
          inContext { node =>
            signal.mapTo(node.ref.value) --> writer
          },
        )
      case ("Negation", _, signal) =>
        val writer =
          clueBrushVar.writer.contracollect[String](Function.unlift { s =>
            s.toIntOption.map(Clue.Negation(_))
          })
        input(
          typ("text"),
          onInput.mapToValue --> writer,
          inContext { node =>
            signal.mapTo(node.ref.value) --> writer
          },
        )
      case _ => emptyNode
    }

    div(
      label("brush"),
      select(
        options.map(selectOption),
        onChange.mapToValue --> selected.writer,
      ),
      child <-- dataInput,
      mods.map(_(ctx)),
    )
  }
}
