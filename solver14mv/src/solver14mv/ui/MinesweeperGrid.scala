package solver14mv.ui

import cats.syntax.all.*
import com.raquo.laminar.DomApi
import com.raquo.laminar.api.L
import com.raquo.laminar.api.L.*
import solver14mv.solver.Clue
import solver14mv.solver.SolveResult.CellSafety

import scala.scalajs.js

object MinesweeperGrid {
  trait Styles {
    val root: StrictSignal[String]
    val btn: StrictSignal[String]
    val topLeft: StrictSignal[String]
    val topRight: StrictSignal[String]
    val bottomLeft: StrictSignal[String]
    val bottomRight: StrictSignal[String]
    val str: StrictSignal[String]
    val withAnnotation: StrictSignal[String]
    val wall: StrictSignal[String]
    val withHorizontalArrow: StrictSignal[String]
    val withVerticalArrow: StrictSignal[String]
  }
  val styles = MinesweeperGridModuleCSS.as[Styles]

  trait Context {
    def onClick: EventStream[(Int, Int)]
  }

  def onClick(using ctx: Context): EventStream[(Int, Int)] = ctx.onClick

  type ModFunction = Context ?=> Mod[HtmlElement]

  enum ClueViewModel {
    case Str(str: String)
    case WithAnnotation(str: String, annotation: String)
    case Wall(strs: List[String])
    case WithHorizontalArrow(str: String)
    case WithVerticalArrow(str: String)
  }

  object ClueViewModel {
    def fromClue(clue: Clue): ClueViewModel = clue match {
      case Clue.None => ClueViewModel.Str("")
      case Clue.QuestionMark => ClueViewModel.Str("?")
      case Clue.Flagged => ClueViewModel.Str("🚩")
      case Clue.Vanilla(value) => ClueViewModel.Str(value.toString)
      case Clue.Multiple(value) =>
        ClueViewModel.WithAnnotation(value.toString, "M")
      case Clue.Liar(value) => ClueViewModel.WithAnnotation(value.toString, "L")
      case Clue.Wall(value) =>
        ClueViewModel.Wall(value.sorted.map(_.toString))
      case Clue.Negation(value) =>
        ClueViewModel.WithAnnotation(value.toString, "N")
      case Clue.Cross(value) =>
        ClueViewModel.WithAnnotation(value.toString, "X")
      case Clue.Partition(value) =>
        ClueViewModel.WithAnnotation(value.toString, "P")
      case Clue.Eyesight(value) =>
        ClueViewModel.WithAnnotation(value.toString, "E")
      case Clue.MiniCross(value) =>
        ClueViewModel.WithAnnotation(value.toString, "X'")
      case Clue.Knight(value) =>
        ClueViewModel.WithAnnotation(value.toString, "K")
      case Clue.LongestWall(value) =>
        ClueViewModel.WithAnnotation(value.toString, "W'")
      case Clue.EyesightPrime(value) =>
        if (value > 0) ClueViewModel.WithHorizontalArrow(value.toString)
        else if (value < 0) ClueViewModel.WithVerticalArrow((-value).toString)
        else ClueViewModel.WithAnnotation("0", "E'")
    }
  }

  def apply(
      clues: Signal[Array[Array[Clue]]],
      cellSafety: Signal[Array[Array[CellSafety]]],
  )(mods: ModFunction*): HtmlElement = {
    val onClickBus = EventBus[(Int, Int)]()
    val ctx = new Context {
      override def onClick: EventStream[(Int, Int)] = onClickBus.stream
    }

    val gridSignal: Signal[Array[Array[(Clue, CellSafety)]]] =
      Signal.combine(clues, cellSafety).map { case (clue, cellSafety) =>
        clue
          .zip(cellSafety)
          .map((clueRow, cellSafetyRow) => clueRow.zip(cellSafetyRow))
      }

    val buttons = gridSignal
      .map { grid =>
        val m = grid.length
        val n = grid.lift(0).map(_.length).getOrElse(0)
        grid.zipWithIndex.flatMap { case (arr, i) =>
          arr.zipWithIndex.map { case ((clue, cellSafety), j) =>
            (
              m = m,
              n = n,
              i = i,
              j = j,
              model = ClueViewModel.fromClue(clue),
              safety = cellSafety,
            )
          }
        }.toSeq
      }
      .split((m, n, i, j, _, _) => (m, n, i, j)) {
        case ((m, n, i, j), _, signal) =>
          import ClueViewModel.*
          val content = signal
            .map(_.model)
            .splitMatchOne
            .handleType[Str] { case (_, signal) =>
              div(cls <-- styles.str, span(text <-- signal.map(_.str)))
            }
            .handleType[WithAnnotation] { case (_, signal) =>
              div(
                cls <-- styles.withAnnotation,
                span(text <-- signal.map(_.str)),
                span(text <-- signal.map(_.annotation)),
              )
            }
            .handleType[Wall] { case (_, signal) =>
              div(
                cls <-- styles.wall,
                children <-- signal.map(_.strs.padTo(3, "0")).splitByIndex {
                  case (_, _, signal) =>
                    span(text <-- signal, dataAttr("content") <-- signal)
                },
                div("W"),
              )
            }
            .handleType[WithHorizontalArrow] { case (_, signal) =>
              div(
                cls <-- styles.withHorizontalArrow,
                span(text <-- signal.map { case WithHorizontalArrow(str) =>
                  str
                }),
                foreignSvgElement(
                  DomApi.unsafeParseSvgString(
                    """<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 15 15" width="24" height="24" fill="currentColor" style="opacity:1;"><path  d="M10.182 4.682a.45.45 0 0 1 .566-.058l.07.058l2.5 2.5l.058.07a.45.45 0 0 1 0 .496l-.058.07l-2.5 2.5a.45.45 0 0 1-.636-.636l1.731-1.732H3.087l1.731 1.732l.058.07a.451.451 0 0 1-.624.624l-.07-.058l-2.5-2.5a.45.45 0 0 1 0-.636l2.5-2.5l.07-.058a.45.45 0 0 1 .624.624l-.058.07L3.087 7.05h8.826l-1.731-1.732l-.058-.07a.45.45 0 0 1 .058-.566"/></svg>"""
                  )
                ),
              )
            }
            .handleType[WithVerticalArrow] { case (_, signal) =>
              div(
                cls <-- styles.withVerticalArrow,
                span(text <-- signal.map { case WithVerticalArrow(str) =>
                  str
                }),
                foreignSvgElement(
                  DomApi.unsafeParseSvgString(
                    """<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 15 15" width="24" height="24" fill="currentColor" style="opacity:1;"><path  d="M7.252 1.624a.45.45 0 0 1 .566.058l2.5 2.5l.058.07a.45.45 0 0 1-.624.624l-.07-.057L7.95 3.087v8.826l1.731-1.731a.45.45 0 0 1 .637.637l-2.5 2.5a.45.45 0 0 1-.637 0l-2.5-2.5l-.057-.07a.45.45 0 0 1 .624-.625l.07.058l1.732 1.731V3.087L5.318 4.82a.45.45 0 0 1-.637-.637l2.5-2.5z"/></svg>"""
                  )
                ),
              )
            }
            .toSignal

          val clsSignal =
            if (i === 0 && j === 0)
              Signal
                .combine(styles.btn, styles.topLeft)
                .map((s1, s2) => List(s1, s2))
            else if (i === 0 && j === n - 1)
              Signal
                .combine(styles.btn, styles.topRight)
                .map((s1, s2) => List(s1, s2))
            else if (i === m - 1 && j === 0)
              Signal
                .combine(styles.btn, styles.bottomLeft)
                .map((s1, s2) => List(s1, s2))
            else if (i === m - 1 && j === n - 1)
              Signal
                .combine(styles.btn, styles.bottomRight)
                .map((s1, s2) => List(s1, s2))
            else styles.btn.map(List(_))

          button(
            cls <-- clsSignal,
            child <-- content,
            dataAttr("safety") <-- signal.map(_.safety match {
              case CellSafety.Indeterminate => "0"
              case CellSafety.Safe => "1"
              case CellSafety.Mine => "-1"
            }),
            L.onClick.mapTo((i, j)) --> onClickBus,
          )
      }

    val columnsSignal = clues.map { grid =>
      grid.lift(0).map(_.length).getOrElse(0)
    }

    div(
      cls <-- styles.root,
      styleProp[Int]("--columns") <-- columnsSignal,
      children <-- buttons,
      mods.map(_(using ctx)),
    )
  }
}
