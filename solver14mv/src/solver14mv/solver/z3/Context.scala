package solver14mv.solver.z3

import cats.effect.Resource
import cats.effect.Sync

opaque type Context = raw.Context

object Context {
  def apply[F[_]: Sync](config: Config): Resource[F, Context] = Resource.make(
    Sync[F].delay(raw.z3.mk_context(config.getRaw))
  )(context => Sync[F].delay(raw.z3.del_context(context)))

  def apply[F[_]: Sync]: Resource[F, Context] = for {
    (config, delConfig) <- Resource.eval(Config[F].allocated)
    ctx <- Context[F](config)
    // we can delete the config after creating the context
    _ <- Resource.eval(delConfig)
  } yield ctx

  extension (ctx: Context) {
    def getRaw: raw.Context = ctx
  }
}
