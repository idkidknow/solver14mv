package solver14mv.ui.components

import com.raquo.laminar.api.L.*
import japgolly.scalajs.react.Renderable
import scala.scalajs.js
import japgolly.scalajs.react.ReactRoot
import japgolly.scalajs.react.ReactDOMClient

object react {
  def wrap[A: Renderable, P](
      aFn: P => A,
      props: Signal[P],
  ): HtmlElement = {
    var root = Option.empty[ReactRoot]
    div(
      onMountBind { ctx =>
        val node = ctx.thisNode
        val r = ReactDOMClient.createRoot(node.ref)
        root = Some(r)
        props --> { props =>
          val comp = aFn(props)
          root.foreach { r =>
            r.render(comp)
          }
        }
      },
      onUnmountCallback { _ =>
        root.foreach(r => r.unmount())
      },
    )
  }
}
