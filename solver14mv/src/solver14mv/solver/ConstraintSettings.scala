package solver14mv.solver

import scala.scalajs.js

final case class ConstraintSettings(
    cluesAreNotMines: Boolean = false,
    clueEqNeighboring8: Boolean = false,
    totalMineCountEq: Option[Int] = None,
    noTriplets: Boolean = false,
    mines8Connected: Boolean = false,
    quadGe1: Boolean = false,
    mines4Connected: Boolean = false,
    mines4ConnectedToOutside: Boolean = false,
    nonMines4Connected: Boolean = false,
    minesNeighboring4Eq1: Boolean = false,
    minesNeighboring4Le2: Boolean = false,
) {
  def toRaw: ConstraintSettings.Raw = new ConstraintSettings.Raw {
    val clues_are_not_mines = cluesAreNotMines
    val clue_eq_neighboring_8 = clueEqNeighboring8
    val total_mine_count_eq = totalMineCountEq.getOrElse(0)
    val no_triplets = noTriplets
    val mines_8_connected = mines8Connected
    val quad_ge_1 = quadGe1
    val mines_4_connected = mines4Connected
    val mines_4_connected_to_outside = mines4ConnectedToOutside
    val non_mines_4_connected = nonMines4Connected
    val mines_neighboring_4_eq_1 = minesNeighboring4Eq1
    val mines_neighboring_4_le_2 = minesNeighboring4Le2
  }
}

object ConstraintSettings {
  trait Raw extends js.Object {
    val clues_are_not_mines: Boolean
    val clue_eq_neighboring_8: Boolean
    val total_mine_count_eq: Int
    val no_triplets: Boolean
    val mines_8_connected: Boolean
    val quad_ge_1: Boolean
    val mines_4_connected: Boolean
    val mines_4_connected_to_outside: Boolean
    val non_mines_4_connected: Boolean
    val mines_neighboring_4_eq_1: Boolean
    val mines_neighboring_4_le_2: Boolean
  }
}
