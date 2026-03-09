package solver14mv.solver.minizinc

import scala.scalajs.js
import scala.scalajs.js.annotation.JSImport

object MznStrings {
  val files: Map[String, String] = Map(
    "def.mzn" -> `def`,
    "clues_are_not_mines.mzn" -> cluesAreNotMines,
    "clue_eq_neighboring_8.mzn" -> clueEqNeighboring8,
    "total_mine_count_eq.mzn" -> totalMineCountEq,
    "no_triplets.mzn" -> noTriplets,
    "8_connected.mzn" -> EightConnected,
    "quad_ge_1.mzn" -> quadGe1,
    "4_connected.mzn" -> FourConnected,
    "mines_neighboring_4_eq_1.mzn" -> minesNeighboring4Eq1,
    "mines_neighboring_4_le_2.mzn" -> minesNeighboring4Le2,
    "solver14mv.mzn" -> solver14mv,
  )

  @js.native
  @JSImport("@scala/minizinc/solver14mv.mzn?raw", JSImport.Default)
  def solver14mv: String = js.native

  @js.native
  @JSImport("@scala/minizinc/def.mzn?raw", JSImport.Default)
  def `def`: String = js.native

  @js.native
  @JSImport("@scala/minizinc/clues_are_not_mines.mzn?raw", JSImport.Default)
  def cluesAreNotMines: String = js.native

  @js.native
  @JSImport("@scala/minizinc/clue_eq_neighboring_8.mzn?raw", JSImport.Default)
  def clueEqNeighboring8: String = js.native

  @js.native
  @JSImport("@scala/minizinc/total_mine_count_eq.mzn?raw", JSImport.Default)
  def totalMineCountEq: String = js.native

  @js.native
  @JSImport("@scala/minizinc/no_triplets.mzn?raw", JSImport.Default)
  def noTriplets: String = js.native

  @js.native
  @JSImport("@scala/minizinc/8_connected.mzn?raw", JSImport.Default)
  def EightConnected: String = js.native

  @js.native
  @JSImport("@scala/minizinc/quad_ge_1.mzn?raw", JSImport.Default)
  def quadGe1: String = js.native

  @js.native
  @JSImport("@scala/minizinc/4_connected.mzn?raw", JSImport.Default)
  def FourConnected: String = js.native

  @js.native
  @JSImport(
    "@scala/minizinc/mines_neighboring_4_eq_1.mzn?raw",
    JSImport.Default,
  )
  def minesNeighboring4Eq1: String = js.native

  @js.native
  @JSImport(
    "@scala/minizinc/mines_neighboring_4_le_2.mzn?raw",
    JSImport.Default,
  )
  def minesNeighboring4Le2: String = js.native
}
