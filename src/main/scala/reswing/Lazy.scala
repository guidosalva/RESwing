package reswing

/**
 * Provides lazy values that can be checked for if they already hold a defined
 * value, i.e. if they have already been accessed
 */
final class Lazy[+T](init: => T) {
  private[this] var defined = false
  private[this] lazy val value = init
  
  def isDefined = defined
  def apply() = { defined = true; value }
}

object Lazy {
  def apply[T](value: => T) = new Lazy(value)
}
