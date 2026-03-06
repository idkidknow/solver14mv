package solver14mv.ui

import com.raquo.laminar.api.L.*

object NumberInput {
  def apply(num: Var[Int], min: Int, max: Int): HtmlElement = {
    input(
      typ("number"),
      controlled(
        value <-- num.signal.map(_.toString),
        onInput.mapToValue
          .map(
            _.toIntOption.map(_.max(min).min(max))
          )
          .collect { case Some(value) => value } --> num.writer,
      ),
    )
  }
}
