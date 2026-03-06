package solver14mv.ui

import com.raquo.laminar.api.L.*

object ConstraintEditor {
  final case class ConstraintSettings(
      clueEqNeighboring8: Boolean,
      cluesAreNotMine: Boolean,
      noTriplets: Boolean,
      totalMineCount: Option[Int],
  )

  trait Context {
    def constraints: Signal[ConstraintSettings]
  }

  type ModFunction = Context => Mod[HtmlElement]

  def apply(mods: ModFunction*): HtmlElement = {
    val constraintsVar = Var(
      ConstraintSettings(false, false, false, None)
    ).distinct
    val ctx = new Context {
      override def constraints: Signal[ConstraintSettings] =
        constraintsVar.signal
    }
    val clueEqNeighboring8 = constraintsVar.zoomLazy(_.clueEqNeighboring8) {
      (c, b) =>
        c.copy(clueEqNeighboring8 = b)
    }
    val cluesAreNotMine = constraintsVar.zoomLazy(_.cluesAreNotMine) { (c, b) =>
      c.copy(cluesAreNotMine = b)
    }
    val totalMineCount = constraintsVar.zoomLazy(_.totalMineCount) { (c, cnt) =>
      c.copy(totalMineCount = cnt)
    }
    val totalMineCountInput = Var(0)
    val noTriplets = constraintsVar.zoomLazy(_.noTriplets) { (c, b) =>
      c.copy(noTriplets = b)
    }
    div(
      div(
        input(
          typ("checkbox"),
          onClick.mapToChecked --> clueEqNeighboring8,
        ),
        label("clue = neighboring 8"),
      ),
      div(
        input(
          typ("checkbox"),
          onClick.mapToChecked --> cluesAreNotMine,
        ),
        label("clues are not mine"),
      ),
      div(
        input(
          typ("checkbox"),
          onClick.mapToChecked.map(
            if (_) Some(totalMineCountInput.now()) else None
          ) --> totalMineCount,
        ),
        label("total mine count = "),
        NumberInput(totalMineCountInput, 0, 100),
        totalMineCountInput.signal --> totalMineCount.updater[Int](
          (prev, cnt) => prev.map(_ => cnt)
        ),
      ),
      div(
        input(
          typ("checkbox"),
          onClick.mapToChecked --> noTriplets,
        ),
        label("no triplets"),
      ),
      mods.map(_(ctx)),
    )
  }
}
