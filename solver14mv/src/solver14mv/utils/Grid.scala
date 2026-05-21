package solver14mv.utils

import scala.scalajs.js

final class Grid[A] private (inner: js.Array[js.Array[A]]) {
  def m: Int = inner.length
  def n: Int = inner.lift(0).map(_.length).getOrElse(0)
  def mn: (Int, Int) = (m, n)

  def toJs: js.Array[js.Array[A]] = inner.map(_.map(identity))
  def unsafeToJs: js.Array[js.Array[A]] = inner

  def apply(i: Int, j: Int): A = inner(i)(j)

  def updated2D(i: Int, j: Int, elem: A): Grid[A] = {
    val copy = toJs
    copy(i)(j) = elem
    new Grid[A](copy)
  }

  def zip[B](other: Grid[B]): Grid[(A, B)] = {
    val arr = inner.zip(other.unsafeToJs).map { case (rowA, rowB) =>
      rowA.zip(rowB)
    }
    new Grid(arr)
  }
}

object Grid {
  def apply[A](arr: js.Array[js.Array[A]]): Grid[A] =
    new Grid(arr.map(_.map(identity)))

  def fill[A](m: Int, n: Int)(elem: => A): Grid[A] = {
    def createRow() = {
      val row = new js.Array[A](n)
      for (j <- 0 until n) row(j) = elem
      row
    }
    val arr = new js.Array[js.Array[A]](m)
    for (i <- 0 until m) arr(i) = createRow()
    new Grid(arr)
  }

  def empty[A]: Grid[A] = new Grid(new js.Array())
}
