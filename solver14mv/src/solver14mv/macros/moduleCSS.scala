package solver14mv.macros

import scala.quoted.*
import scala.scalajs.js
import com.raquo.laminar.api.L

inline def moduleCSS[A](path: String, obj: js.Object): A = ${
  moduleCSSImpl[A]('path, 'obj)
}

def moduleCSSImpl[A: Type](path: Expr[String], obj: Expr[js.Object])(using
    Quotes
): Expr[A] = {
  import quotes.reflect.*
  val parent = TypeRepr.of[A]
  val fields = parent.typeSymbol.declaredFields
  def decls(cls: Symbol) = fields.map { field =>
    field.tree match {
      case ValDef(name, tpt, _) =>
        if (
          !TypeRepr.of[L.StrictSignal[String]].derivesFrom(tpt.tpe.typeSymbol)
        ) {
          report.errorAndAbort("should return StrictSignal[String]")
        }
        Symbol.newVal(cls, name, tpt.tpe, Flags.Override, Symbol.noSymbol)
      case _ => report.errorAndAbort("should be fields only")
    }
  }
  val cls = Symbol.newClass(
    Symbol.spliceOwner,
    parent.typeSymbol.name + "Impl",
    List(TypeRepr.of[Object], parent),
    decls,
    None,
  )
  val vals = cls.declaredFields.map { field =>
    given Quotes = field.asQuotes
    import quotes.reflect.*
    val name = Expr(field.name)
    val body = '{
      val str: String = $obj
        .asInstanceOf[js.Dynamic]
        .selectDynamic($name)
        .asInstanceOf
      if (js.`import`.meta.hot != null) {
        val varStr = L.Var(str)
        val _ = js.`import`.meta.hot.accept(
          $path.asInstanceOf,
          { (newObj: js.Dynamic) =>
            val newStr: String = newObj.selectDynamic($name).asInstanceOf
            varStr.set(newStr)
          },
        )
        varStr.signal
      } else {
        L.Val(str)
      }
    }
    ValDef(
      field,
      Some(body.asTerm),
    )
  }
  val clsDef = ClassDef(cls, List(TypeTree.of[Object], TypeTree.of[A]), vals)
  val newCls = Typed(
    Apply(Select(New(TypeIdent(cls)), cls.primaryConstructor), Nil),
    TypeTree.of[A],
  )

  Block(List(clsDef), newCls).asExprOf[A]
}
