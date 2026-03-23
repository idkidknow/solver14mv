package solver14mv.ui.components

import com.raquo.laminar.api.L
import japgolly.scalajs.react.*
import japgolly.scalajs.react.vdom.*
import japgolly.scalajs.react.vdom.VdomElement
import japgolly.scalajs.react.vdom.html_<^.*
import org.scalajs.dom

object react {
  def wrap[A: Renderable, P](
      aFn: P => A,
      props: L.Signal[P],
  ): L.HtmlElement = {
    import L.*
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

  def fromLaminar[P](fn: L.Signal[P] => L.HtmlElement) = {
    class Backend(val $ : BackendScope[P, Unit]) {
      val ref = Ref[dom.HTMLDivElement]
      val propsVar = L.Var($.props.runNow())
      var lRoot: L.RootNode = null
      def render(): VdomElement = <.div().withRef(ref)
      def mount() = ref.foreach { elem =>
        lRoot = L.render(elem, fn(propsVar.signal))
      }
      def update(p: P) = propsVar.set(p)
      def unmount() = lRoot.unmount()
    }
    ScalaComponent
      .builder[P]
      .backend(new Backend(_))
      .render(_.backend.render())
      .componentDidMount(_.backend.mount())
      .componentWillUnmount(c => Callback(c.backend.unmount()))
      .componentDidUpdate(c => Callback(c.backend.update(c.currentProps)))
      .build
  }
}
