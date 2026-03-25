package solver14mv.ui.components.react

import cats.effect.SyncIO
import cats.syntax.all.*
import com.raquo.laminar.api.L
import com.raquo.laminar.nodes.DetachedRoot
import japgolly.scalajs.react.*
import japgolly.scalajs.react.vdom.*
import japgolly.scalajs.react.vdom.html_<^.*
import org.scalajs.dom

object Laminar {
  def apply[P](
      elem: L.Signal[P] => L.Element
  ): ScalaFnComponent[P, CtorType.Props] = ScalaFnComponent[P] { props =>
    for {
      ref <- useRefToVdom[dom.Element]
      propsVar <- useRef(L.Var(props))
      laminarRoot <- useRef(Option.empty[DetachedRoot[L.Element]])
      _ <- useLayoutEffectOnMount {
        val mount: SyncIO[Unit] = ref.get.flatMap {
          case None => ().pure
          case Some(div) =>
            for {
              root <- SyncIO(
                L.renderDetached(elem(propsVar.value.signal), true)
              )
              _ <- laminarRoot.set(Some(root))
              _ <- SyncIO(div.replaceWith(root.ref))
            } yield ()
        }
        val unmount: SyncIO[Unit] = laminarRoot.get.flatMap {
          case None => ().pure
          case Some(root) =>
            for {
              _ <- SyncIO(root.deactivate())
              div <- ref.get
              _ <- div match {
                case None => ().pure[SyncIO]
                case Some(div) => SyncIO(root.ref.replaceWith(div))
              }
            } yield ()
        }
        mount.map(_ => unmount)
      }
      _ <- useLayoutEffect(SyncIO {
        propsVar.value.set(props)
      })
    } yield <.div().withRef(ref)
  }
}
