package solver14mv.ui

import com.raquo.laminar.api.L.*
import solver14mv.solver.ConstraintSettings

object ConstraintEditor {

  trait Context {
    def constraints: Signal[ConstraintSettings]
  }

  type ModFunction = Context => Mod[HtmlElement]

  def apply(mods: ModFunction*): HtmlElement = {
    val constraintsVar = Var(ConstraintSettings()).distinct
    val ctx = new Context {
      override def constraints: Signal[ConstraintSettings] =
        constraintsVar.signal
    }
    val cluesAreNotMines = constraintsVar.zoomLazy(_.cluesAreNotMines) {
      (c, b) =>
        c.copy(cluesAreNotMines = b)
    }
    val clueEqNeighboring8 = constraintsVar.zoomLazy(_.clueEqNeighboring8) {
      (c, b) =>
        c.copy(clueEqNeighboring8 = b)
    }
    val totalMineCountEq = constraintsVar.zoomLazy(_.totalMineCountEq) {
      (c, cnt) =>
        c.copy(totalMineCountEq = cnt)
    }
    val totalMineCountInput = Var(0)
    val noTriplets = constraintsVar.zoomLazy(_.noTriplets) { (c, b) =>
      c.copy(noTriplets = b)
    }
    val mines8Connected = constraintsVar.zoomLazy(_.mines8Connected) { (c, b) =>
      c.copy(mines8Connected = b)
    }
    val quadGe1 = constraintsVar.zoomLazy(_.quadGe1) { (c, b) =>
      c.copy(quadGe1 = b)
    }
    val mines4Connected = constraintsVar.zoomLazy(_.mines4Connected) { (c, b) =>
      c.copy(mines4Connected = b)
    }
    val mines4ConnectedToOutside =
      constraintsVar.zoomLazy(_.mines4ConnectedToOutside) { (c, b) =>
        c.copy(mines4ConnectedToOutside = b)
      }
    val nonMines4Connected = constraintsVar.zoomLazy(_.nonMines4Connected) {
      (c, b) =>
        c.copy(nonMines4Connected = b)
    }
    val minesNeighboring4Eq1 = constraintsVar.zoomLazy(_.minesNeighboring4Eq1) {
      (c, b) =>
        c.copy(minesNeighboring4Eq1 = b)
    }
    val minesNeighboring4Le2 = constraintsVar.zoomLazy(_.minesNeighboring4Le2) {
      (c, b) =>
        c.copy(minesNeighboring4Le2 = b)
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
          onClick.mapToChecked --> cluesAreNotMines,
        ),
        label("clues are not mines"),
      ),
      div(
        input(
          typ("checkbox"),
          onClick.mapToChecked.map(
            if (_) Some(totalMineCountInput.now()) else None
          ) --> totalMineCountEq,
        ),
        label("total mine count = "),
        NumberInput(totalMineCountInput, 0, 100),
        totalMineCountInput.signal --> totalMineCountEq.updater[Int](
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
      div(
        input(
          typ("checkbox"),
          onClick.mapToChecked --> mines8Connected,
        ),
        label("mines 8-connected"),
      ),
      div(
        input(
          typ("checkbox"),
          onClick.mapToChecked --> quadGe1,
        ),
        label("quad ≥ 1"),
      ),
      div(
        input(
          typ("checkbox"),
          onClick.mapToChecked --> mines4Connected,
        ),
        label("mines 4-connected"),
      ),
      div(
        input(
          typ("checkbox"),
          onClick.mapToChecked --> mines4ConnectedToOutside,
        ),
        label("mines 4-connected to outside"),
      ),
      div(
        input(
          typ("checkbox"),
          onClick.mapToChecked --> nonMines4Connected,
        ),
        label("non-mines 4-connected"),
      ),
      div(
        input(
          typ("checkbox"),
          onClick.mapToChecked --> minesNeighboring4Eq1,
        ),
        label("mines' neighboring 4 = 1"),
      ),
      div(
        input(
          typ("checkbox"),
          onClick.mapToChecked --> minesNeighboring4Le2,
        ),
        label("mines' neighboring 4 ≤ 2"),
      ),
      mods.map(_(ctx)),
    )
  }
}
