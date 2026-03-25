package solver14mv.ui.components.react

import cats.effect.SyncIO
import com.raquo.airstream.ownership.ManualOwner
import com.raquo.laminar.api.L
import japgolly.scalajs.react.*
import japgolly.scalajs.react.vdom.*
import japgolly.scalajs.react.vdom.html_<^.*
import org.scalajs.dom
import solver14mv.ui.components.react.PortalHub.PortalKey
import solver14mv.ui.components.react.PortalHub.PortalProps

/** Behave like `Var[Map[PortalKey, PortalProps]]` but restrict the use of keys
 */
trait PortalHub extends L.SignalSource[Map[PortalKey, PortalProps]] {
  def createPortal(props: PortalProps): PortalKey
  def removePortal(key: PortalKey): Unit
  override def toObservable: L.Signal[Map[String, PortalProps]]
}

object PortalHub {
  type PortalKey = String

  /** @param child
   *    Signal of VdomNode which renders only one node in real DOM, accepting
   *    the ref
   *  @param ref
   *    emit the ref when in useLayoutEffect
   */
  final case class PortalProps(
      child: L.Signal[Ref.ToVdom[dom.Element] => VdomNode],
      container: dom.Element,
      ref: L.Sink[dom.Element],
  )

  def apply(): PortalHub = {
    val portals = L.Var(Map.empty[PortalKey, PortalProps])
    var nextKey = 0 // scalafix:ok
    new PortalHub {
      override def createPortal(props: PortalProps): PortalKey = {
        val key = nextKey.toString
        nextKey += 1
        portals.update(_.updated(key, props))
        key
      }
      override def removePortal(key: PortalKey): Unit =
        portals.update(_.removed(key))
      override def toObservable: L.Signal[Map[String, PortalProps]] =
        portals.signal
    }
  }

  def portalDest(
      hub: PortalHub,
      vdomChild: L.Signal[Ref.ToVdom[dom.Element] => VdomNode],
  ): L.Mod[L.Element] = {
    import L.*
    val childBus: EventBus[dom.Element] = EventBus[dom.Element]()
    val childStream: EventStream[Element] = childBus.stream.map {
      case elem: dom.HTMLElement => foreignHtmlElement(elem)
      case elem: dom.SVGElement => foreignSvgElement(elem)
    }
    modSeq(
      child <-- childStream,
      onMountUnmountCallbackWithState(
        mount = mountCtx => {
          val container = mountCtx.thisNode.ref
          val ref: Sink[dom.Element] = childBus.writer
          val key = hub.createPortal(PortalProps(vdomChild, container, ref))
          key
        },
        unmount = (_, keyOpt) => {
          keyOpt.foreach(key => hub.removePortal(key))
        },
      ),
    )
  }

  extension (hub: PortalHub) {
    def dest(
        vdomChild: L.Signal[Ref.ToVdom[dom.Element] => VdomNode]
    ): L.Mod[L.Element] = portalDest(hub, vdomChild)
  }

  /** React Component that instantiates portals */
  val Backend = ScalaFnComponent[PortalHub] { hub =>
    val Portal = ScalaFnComponent[PortalProps] { props =>
      for {
        owner <- useRef(Option.empty[ManualOwner])
        child <- useState(
          VdomNode(null)
        ) // scalafix:ok DisableSyntax.null; VdomNode(null) is valid
        ref <- useRefToVdom[dom.Element]
        prevRef <- useRef[dom.Element](null) // scalafix:ok DisableSyntax.null
        _ <- useLayoutEffectOnMount {
          val mount: SyncIO[Unit] = for {
            _ <- owner.set(Some(ManualOwner()))
            _ <- SyncIO {
              given L.Owner = owner.value.get
              val _ = props.child.addObserver(L.Observer { childFn =>
                child.setState(childFn(ref)).unsafeRunSync()
              })
            }
          } yield ()
          val unmount: SyncIO[Unit] =
            owner.foreach(_.foreach(_.killSubscriptions()))
          mount.map(_ => unmount)
        }
        _ <- useLayoutEffect(ref.foreach { elem =>
          if (prevRef.value != elem) { // scalafix:ok DisableSyntax.!=; comparing Element references
            prevRef.value = elem
            props.ref.toObserver.onNext(elem)
          }
        })
      } yield {
        ReactPortal(child.value.rawNode, props.container)
      }
    }

    for {
      owner <- useRef(Option.empty[ManualOwner])
      portals <- useState(Map.empty[String, PortalProps])
      _ <- useLayoutEffectOnMount {
        val mount: SyncIO[Unit] = for {
          _ <- owner.set(Some(ManualOwner()))
          _ <- SyncIO {
            given L.Owner = owner.value.get
            val _ = hub.toObservable.addObserver(L.Observer { map =>
              portals.setState(map).unsafeRunSync()
            })
          }
        } yield ()
        val unmount: SyncIO[Unit] =
          owner.foreach(_.foreach(_.killSubscriptions()))
        mount.map(_ => unmount)
      }
    } yield {
      portals.value.map { case (key, props) =>
        Portal.withKey(key).apply(props)
      }.toReactFragment
    }
  }

  lazy val global: PortalHub = {
    val hub = PortalHub()
    val div = dom.document.createElement("div")
    val _ = dom.document.body.appendChild(div)
    val root = ReactDOMClient.createRoot(div)
    root.render(Backend(hub))
    hub
  }

  def globalDest(
      vdomChild: L.Signal[Ref.ToVdom[dom.Element] => VdomNode]
  ): L.Mod[L.Element] = portalDest(global, vdomChild)

}
