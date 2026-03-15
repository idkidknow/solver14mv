package solver14mv.ui

import cats.syntax.all.*
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
    val options =
      Seq(
        "None",
        "?",
        "Vanilla",
        "Multiple",
        "Liar",
        "Wall",
        "Negation",
        "Cross",
        "Partition",
        "Eyesight",
        "MiniCross",
        "Knight",
        "LongestWall",
      )

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
      case ("Wall", _, signal) =>
        val writer =
          clueBrushVar.writer.contracollect[List[String]](Function.unlift {
            lst =>
              lst
                .filter(_.nonEmpty)
                .traverse(s => s.toIntOption)
                .map(_.filter(_ =!= 0))
                .map(Clue.Wall(_))
          })
        val vars = List.fill(4)(Var(""))
        val combined = Signal.combineSeq(vars.map(_.signal)).map(_.toList)
        val inputs = List.tabulate(4) { i =>
          input(
            typ("text"),
            onInput.mapToValue --> vars(i).writer,
          )
        }
        div(
          inputs,
          combined --> writer,
          signal.sample(combined) --> writer,
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
      case ("Cross", _, signal) =>
        val writer =
          clueBrushVar.writer.contracollect[String](Function.unlift { s =>
            s.toIntOption.map(Clue.Cross(_))
          })
        input(
          typ("text"),
          onInput.mapToValue --> writer,
          inContext { node =>
            signal.mapTo(node.ref.value) --> writer
          },
        )
      case ("Partition", _, signal) =>
        val writer =
          clueBrushVar.writer.contracollect[String](Function.unlift { s =>
            s.toIntOption.map(Clue.Partition(_))
          })
        input(
          typ("text"),
          onInput.mapToValue --> writer,
          inContext { node =>
            signal.mapTo(node.ref.value) --> writer
          },
        )
      case ("Eyesight", _, signal) =>
        val writer =
          clueBrushVar.writer.contracollect[String](Function.unlift { s =>
            s.toIntOption.map(Clue.Eyesight(_))
          })
        input(
          typ("text"),
          onInput.mapToValue --> writer,
          inContext { node =>
            signal.mapTo(node.ref.value) --> writer
          },
        )
      case ("MiniCross", _, signal) =>
        val writer =
          clueBrushVar.writer.contracollect[String](Function.unlift { s =>
            s.toIntOption.map(Clue.MiniCross(_))
          })
        input(
          typ("text"),
          onInput.mapToValue --> writer,
          inContext { node =>
            signal.mapTo(node.ref.value) --> writer
          },
        )
      case ("Knight", _, signal) =>
        val writer =
          clueBrushVar.writer.contracollect[String](Function.unlift { s =>
            s.toIntOption.map(Clue.Knight(_))
          })
        input(
          typ("text"),
          onInput.mapToValue --> writer,
          inContext { node =>
            signal.mapTo(node.ref.value) --> writer
          },
        )
      case ("LongestWall", _, signal) =>
        val writer =
          clueBrushVar.writer.contracollect[String](Function.unlift { s =>
            s.toIntOption.map(Clue.LongestWall(_))
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
