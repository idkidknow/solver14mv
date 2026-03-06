package solver14mv.solver.z3

import cats.effect.Resource
import cats.effect.Sync

opaque type Config = raw.Config

object Config {
  def apply[F[_]: Sync]: Resource[F, Config] = Resource.make(
    Sync[F].delay(raw.z3.mk_config())
  )(config => Sync[F].delay(raw.z3.del_config(config)))

  extension (config: Config) {
    def getRaw: raw.Config = config
  }
}
