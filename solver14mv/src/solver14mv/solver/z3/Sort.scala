package solver14mv.solver.z3

import scala.scalajs.js.JSConverters.*

opaque type Sort = raw.Sort

object Sort {
  def int(using ctx: Context): Sort = raw.z3.mk_int_sort(ctx.getRaw)
  def bool(using ctx: Context): Sort = raw.z3.mk_bool_sort(ctx.getRaw)
  def array(domain: Sort, range: Sort)(using ctx: Context): Sort =
    raw.z3.mk_array_sort(ctx.getRaw, domain, range)
  def arrayN(domain: Seq[Sort], range: Sort)(using ctx: Context): Sort =
    raw.z3.mk_array_sort_n(ctx.getRaw, domain.toJSArray, range)

  extension (sort: Sort) {
    def getRaw: raw.Sort = sort
  }
}
