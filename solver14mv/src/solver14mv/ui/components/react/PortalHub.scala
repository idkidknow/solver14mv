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

  enum PortalProps {

    /** No children in real DOM */
    case None(container: dom.Element, node: L.Signal[VdomNode])

    /** Exactly one child in real DOM */
    case One(
        container: dom.Element,
        node: L.Signal[Ref.ToVdom[dom.Element] => VdomNode],
        ref: L.Sink[dom.Element],
    )

    /** Fixed number of children in real DOM */
    case Multiple(
        container: dom.Element,
        count: Int,
        node: L.Signal[PortalProps.MultipleRefSetter => VdomNode],
        ref: L.Sink[IArray[Option[dom.Element]]],
    )
  }

  object PortalProps {
    trait MultipleRefSetter {
      def set(idx: Int, elem: dom.Element): Unit
    }
  }

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

  def portalBind(
      hub: PortalHub,
      vdomChild: L.Signal[VdomNode],
  ): L.Mod[L.Element] = {
    import L.*
    modSeq(
      onMountUnmountCallbackWithState(
        mount = mountCtx => {
          val container = mountCtx.thisNode.ref
          val key = hub.createPortal(
            PortalProps.None(container, vdomChild)
          )
          key
        },
        unmount = (_, keyOpt) => {
          keyOpt.foreach(key => hub.removePortal(key))
        },
      )
    )
  }

  def portalOne(
      hub: PortalHub,
      vdomChild: L.Signal[Ref.ToVdom[dom.Element] => VdomNode],
  ): L.Mod[L.Element] = {
    import L.*
    val childBus: EventBus[dom.Element] = EventBus()
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
          val key = hub.createPortal(
            PortalProps.One(container, vdomChild, ref)
          )
          key
        },
        unmount = (_, keyOpt) => {
          keyOpt.foreach(key => hub.removePortal(key))
        },
      ),
    )
  }

  def portalMultiple(
      hub: PortalHub,
      count: Int,
      vdomChild: L.Signal[PortalProps.MultipleRefSetter => VdomNode],
  ): L.Mod[L.Element] = {
    import L.*
    val childrenBus: EventBus[IArray[Option[dom.Element]]] = EventBus()
    val childMods = Seq.tabulate(count) { i =>
      child <-- childrenBus.stream.map(_.apply(i)).distinct.map {
        case Some(elem: dom.HTMLElement) => foreignHtmlElement(elem)
        case Some(elem: dom.SVGElement) => foreignSvgElement(elem)
        case _ => emptyNode
      }
    }
    modSeq(
      childMods,
      onMountUnmountCallbackWithState(
        mount = mountCtx => {
          val container = mountCtx.thisNode.ref
          val ref: Sink[IArray[Option[dom.Element]]] = childrenBus.writer
          val key = hub.createPortal(
            PortalProps.Multiple(container, count, vdomChild, ref)
          )
          key
        },
        unmount = (_, keyOpt) => {
          keyOpt.foreach(key => hub.removePortal(key))
        },
      ),
    )
  }

  /** React Component that instantiates portals */
  val Backend = ScalaFnComponent[PortalHub] { hub =>
    val PortalNone = ScalaFnComponent[PortalProps.None] { props =>
      for {
        owner <- useRef(Option.empty[ManualOwner])
        child <- useState(
          VdomNode(null)
        ) // scalafix:ok DisableSyntax.null; VdomNode(null) is valid
        _ <- useLayoutEffectOnMount {
          val mount: SyncIO[Unit] = for {
            _ <- owner.set(Some(ManualOwner()))
            _ <- SyncIO {
              given L.Owner = owner.value.get
              val _ = props.node.addObserver(L.Observer { newChild =>
                child.setState(newChild).unsafeRunSync()
              })
            }
          } yield ()
          val unmount: SyncIO[Unit] =
            owner.foreach(_.foreach(_.killSubscriptions()))
          mount.map(_ => unmount)
        }
      } yield {
        ReactPortal(child.value, props.container)
      }
    }

    val PortalOne = ScalaFnComponent[PortalProps.One] { props =>
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
              val _ = props.node.addObserver(L.Observer { childFn =>
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
        ReactPortal(child.value, props.container)
      }
    }

    val PortalMultiple = ScalaFnComponent[PortalProps.Multiple] { props =>
      for {
        owner <- useRef(Option.empty[ManualOwner])
        child <- useState(
          VdomNode(null)
        ) // scalafix:ok DisableSyntax.null; VdomNode(null) is valid
        refs <- useRef(Map.empty[Int, dom.Element])
        _ <- useLayoutEffectOnMount {
          val mount: SyncIO[Unit] = for {
            _ <- owner.set(Some(ManualOwner()))
            _ <- SyncIO {
              val setter: PortalProps.MultipleRefSetter = (idx, elem) => {
                refs.mod(_.updated(idx, elem)).unsafeRunSync()
              }
              given L.Owner = owner.value.get
              val _ = props.node.addObserver(L.Observer { childFn =>
                child.setState(childFn(setter)).unsafeRunSync()
              })
            }
          } yield ()
          val unmount: SyncIO[Unit] =
            owner.foreach(_.foreach(_.killSubscriptions()))
          mount.map(_ => unmount)
        }
        _ <- useLayoutEffect(refs.foreach { elems =>
          val arr = IArray.tabulate(props.count) { i =>
            elems.get(i)
          }
          props.ref.toObserver.onNext(arr)
        })
      } yield {
        ReactPortal(child.value, props.container)
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
        props match {
          case props: PortalProps.None =>
            PortalNone.withKey(key).apply(props)
          case props: PortalProps.One =>
            PortalOne.withKey(key).apply(props)
          case props: PortalProps.Multiple =>
            PortalMultiple.withKey(key).apply(props)
        }
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

  def globalPortalBind(
      vdomChild: L.Signal[VdomNode]
  ): L.Mod[L.Element] = portalBind(global, vdomChild)

  def globalPortalOne(
      vdomChild: L.Signal[Ref.ToVdom[dom.Element] => VdomNode]
  ): L.Mod[L.Element] = portalOne(global, vdomChild)

  def globalPortalMultiple(
      count: Int,
      vdomChild: L.Signal[PortalProps.MultipleRefSetter => VdomNode],
  ): L.Mod[L.Element] = portalMultiple(global, count, vdomChild)

}
