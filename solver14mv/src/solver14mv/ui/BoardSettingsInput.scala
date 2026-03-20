package solver14mv.ui

import com.raquo.laminar.api.L.*
import solver14mv.ui.components.Field
import solver14mv.ui.components.Input
import solver14mv.ui.components.Checkbox

object BoardSettingsInput {
  def apply(): HtmlElement = {
    div(
      Field.Set(
        Field.Legend("legend")("board"),
        Field.Group(
          div(
            cls("grid grid-cols-2 gap-4"),
            Field()(
              Field.Label("row"),
              components.Range(1, 10)(),
            ),
            Field()(
              Field.Label("col"),
              components.Range(1, 10)(),
            ),
          )
        ),
        Field.Group(
          Field("horizontal")(
            Checkbox(),
            Field.Label("Mine counting"),
          ),
          Field()(
            Field.Label("Mine count"),
            Input("number")(
              placeholder("Unknown")
            ),
          ),
        ),
      )
    )
  }
}
