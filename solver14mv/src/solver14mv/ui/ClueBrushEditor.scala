package solver14mv.ui

import cats.syntax.all.*
import com.raquo.laminar.api.L.*
import solver14mv.solver.Clue
import cats.kernel.Eq

object ClueBrushEditor {
  trait Styles {
    val root: StrictSignal[String]
    val item: StrictSignal[String]
    val item1Btn: StrictSignal[String]
    val wallBtn: StrictSignal[String]
  }
  val styles = ClueBrushEditorModuleCSS.as[Styles]

  trait Context {
    def clueBrush: Signal[Clue]
    def addNumber: Observer[Int]
  }

  def clueBrush(using ctx: Context): Signal[Clue] = ctx.clueBrush
  def addNumber(using ctx: Context): Observer[Int] = ctx.addNumber

  type ModFunction = Context ?=> Mod[HtmlElement]

  def apply(mods: ModFunction*): HtmlElement = {
    val clueBrushVar = Var(Clue.None)
    val addNumberBus = EventBus[Int]()
    val ctx = new Context {
      override def clueBrush: Signal[Clue] =
        clueBrushVar.signal
      override def addNumber: Observer[Int] = addNumberBus.writer
    }

    def item0(
        name: String,
        buttonText: String,
        clue: Clue,
        active: Signal[Boolean],
    ): HtmlElement = {
      div(
        cls <-- styles.item,
        div(name),
        button(
          buttonText,
          dataAttr("active") <-- active.map(if (_) "1" else "0"),
          onFocus.mapTo(clue) --> clueBrushVar.writer,
        ),
      )
    }

    def item1(
        name: String,
        clue: Int => Clue,
        active: Signal[Boolean],
    ): HtmlElement = {
      val valueVar = Var(0)
      val updateBrush = EventBus[Unit]()
      div(
        cls <-- styles.item,
        div(name),
        button(
          cls <-- styles.item1Btn,
          dataAttr("active") <-- active.map(if (_) "1" else "0"),
          input(
            typ("text"),
            controlled(
              value <-- valueVar.signal.mapLazy(_.toString),
              onInput.mapToValue
                .map(_.toIntOption.map(_.max(0)))
                .collect { case Some(value) => value } --> valueVar.writer,
            ),
            inContext(thisNode => onClick --> { _ => thisNode.ref.select() }),
          ),
          eventProp("focusin").mapTo(()) --> updateBrush,
        ),
        addNumberBus.stream
          .withCurrentValueOf(active)
          .filter(_._2)
          .map(_._1) -->
          valueVar.updater[Int] { case (value, delta) =>
            (value + delta) max 0
          },
        valueVar.signal.changes.map(clue) --> clueBrushVar,
        updateBrush.stream.sample(valueVar).map(clue) --> clueBrushVar,
      )
    }

    val wallItem = {
      val valueVar = Var(IArray.fill(4)(""))
      def toClue(value: IArray[String]): Clue =
        Clue.Wall(
          value.map(_.toIntOption.getOrElse(0)).filter(_ =!= 0).toList
        )

      val updateBrush = EventBus[Unit]()
      def createInput(idx: Int): Input = {
        input(
          typ("text"),
          controlled(
            value <-- valueVar.signal.mapLazy(_(idx)),
            onInput.mapToValue
              .map(_.toIntOption.map(_.max(0).toString).getOrElse("")) -->
              valueVar.updater[String] { case (value, str) =>
                value.updated(idx, str)
              },
          ),
          inContext(thisNode => onClick --> { _ => thisNode.ref.select() }),
        )
      }
      div(
        cls <-- styles.item,
        div("Wall"),
        button(
          cls <-- styles.wallBtn,
          dataAttr("active") <-- clueBrushVar.signal.mapLazy {
            case _: Clue.Wall => "1"
            case _ => "0"
          },
          createInput(0),
          createInput(1),
          createInput(2),
          createInput(3),
          eventProp("focusin").mapTo(()) --> updateBrush,
        ),
        valueVar.signal.changes.map(toClue) --> clueBrushVar,
        updateBrush.stream.sample(valueVar).map(toClue) --> clueBrushVar,
      )
    }

    div(
      cls <-- styles.root,
      item0(
        "None",
        "",
        Clue.None,
        clueBrushVar.signal.mapLazy {
          case Clue.None => true
          case _ => false
        },
      ),
      item0(
        "?",
        "?",
        Clue.QuestionMark,
        clueBrushVar.signal.mapLazy {
          case Clue.QuestionMark => true
          case _ => false
        },
      ),
      item0(
        "Flag",
        "🚩",
        Clue.Flagged,
        clueBrushVar.signal.mapLazy {
          case Clue.Flagged => true
          case _ => false
        },
      ),
      item1(
        "Vanilla",
        Clue.Vanilla(_),
        clueBrushVar.signal.mapLazy {
          case _: Clue.Vanilla => true
          case _ => false
        },
      ),
      item1(
        "Multiple",
        Clue.Multiple(_),
        clueBrushVar.signal.mapLazy {
          case _: Clue.Multiple => true
          case _ => false
        },
      ),
      item1(
        "Liar",
        Clue.Liar(_),
        clueBrushVar.signal.mapLazy {
          case _: Clue.Liar => true
          case _ => false
        },
      ),
      item1(
        "Negation",
        Clue.Negation(_),
        clueBrushVar.signal.mapLazy {
          case _: Clue.Negation => true
          case _ => false
        },
      ),
      item1(
        "Cross",
        Clue.Cross(_),
        clueBrushVar.signal.mapLazy {
          case _: Clue.Cross => true
          case _ => false
        },
      ),
      item1(
        "Partition",
        Clue.Partition(_),
        clueBrushVar.signal.mapLazy {
          case _: Clue.Partition => true
          case _ => false
        },
      ),
      item1(
        "Eyesight",
        Clue.Eyesight(_),
        clueBrushVar.signal.mapLazy {
          case _: Clue.Eyesight => true
          case _ => false
        },
      ),
      item1(
        "Mini Cross",
        Clue.MiniCross(_),
        clueBrushVar.signal.mapLazy {
          case _: Clue.MiniCross => true
          case _ => false
        },
      ),
      item1(
        "Knight",
        Clue.Knight(_),
        clueBrushVar.signal.mapLazy {
          case _: Clue.Knight => true
          case _ => false
        },
      ),
      item1(
        "Longest Wall",
        Clue.LongestWall(_),
        clueBrushVar.signal.mapLazy {
          case _: Clue.LongestWall => true
          case _ => false
        },
      ),
      item1(
        "Eyesight'",
        Clue.EyesightPrime(_),
        clueBrushVar.signal.mapLazy {
          case _: Clue.EyesightPrime => true
          case _ => false
        },
      ),
      wallItem,
      mods.map(_(using ctx)),
    )
  }
}
