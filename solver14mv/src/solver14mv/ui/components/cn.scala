package solver14mv.ui.components

import scala.scalajs.js
import scala.scalajs.js.annotation.JSImport

@js.native
@JSImport("clsx", "clsx")
def clsx(inputs: String): String = js.native

@js.native
@JSImport("tailwind-merge", "twMerge")
def twMerge(inputs: String): String = js.native

def cn(inputs: String): String = {
  twMerge(clsx(inputs))
}
