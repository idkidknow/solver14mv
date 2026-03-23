package solver14mv.ui

import com.raquo.laminar.api.L.*
import solver14mv.ui.components.Field
import solver14mv.ui.components.Input
import solver14mv.ui.components.Checkbox

object BoardSettingsInput {
  trait Styles {
    val root: StrictSignal[String]
    val rowCol: StrictSignal[String]
    val label2: StrictSignal[String]
  }
  val styles = BoardSettingsInputModuleCSS.as[Styles]

  final case class Settings(
      row: Int,
      col: Int,
      mineCount: Option[Int],
  )

  trait Context {
    def settings: Signal[Settings]
  }

  def settings(using ctx: Context): Signal[Settings] = ctx.settings

  type ModFunction = Context ?=> Mod[HtmlElement]

  def apply(mods: ModFunction*): HtmlElement = {
    val row = Var(8)
    val col = Var(8)
    val mineCounting = Var(true)
    val mineCount = Var(26)
    val mineCountUpdater = mineCount
      .updater[(Option[Int], Int, Int)] {
        case (_, (Some(i), row, col)) => 0.max(i.min(row * col))
        case (prev, _) => prev
      }
    val settingsSignal = Signal
      .combine(row, col, mineCounting, mineCount)
      .map { case (row, col, mineCounting, mineCount) =>
        Settings(row, col, if (mineCounting) Some(mineCount) else None)
      }
      .distinct

    val ctx = new Context {
      override def settings: Signal[Settings] = settingsSignal
    }

    div(
      cls <-- styles.root,
      Field.Set(
        Field.Legend("legend")("board"),
        Field.Group(
          div(
            cls <-- styles.rowCol,
            Field()(
              div(
                cls <-- styles.label2,
                Field.Label("row"),
                span(text <-- row.signal),
              ),
              components.Range(1, 10, default = 8)(
                components.Range.value.signal --> row
              ),
            ),
            Field()(
              div(
                cls <-- styles.label2,
                Field.Label("col"),
                span(text <-- col.signal),
              ),
              components.Range(1, 10, default = 8)(
                components.Range.value.signal --> col
              ),
            ),
          ),
          Field()(
            div(
              cls <-- styles.label2,
              Field.Label("Mine count"),
              Checkbox(true)(
                Checkbox.checked --> mineCounting
              ),
            ),
            Input("text")(
              value <-- mineCount.signal.map(_.toString),
              onChange.mapToValue
                .map(s => s.toIntOption)
                .compose(_.withCurrentValueOf(row, col)) --> mineCountUpdater,
              Signal
                .combine(row, col)
                .distinct
                .withCurrentValueOf(mineCount)
                .map((row, col, i) => (Some(i), row, col)) --> mineCountUpdater,
              disabled <-- mineCounting.signal.not,
            ),
          ),
        ),
      ),
      mods.map(_(using ctx)),
    )
  }
}
