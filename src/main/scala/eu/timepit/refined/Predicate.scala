package eu.timepit.refined

/**
 * Type class for validating values of type `T` according to a type-level
 * predicate `P`. The semantics of `P` are defined by the instance of this
 * type class for `P`.
 */
trait Predicate[P, T] { self =>
  /** Checks if `t` satisfies the predicate `P`. */
  def isValid(t: T): Boolean

  /** Returns a string representation of this [[Predicate]] using `t`. */
  def show(t: T): String

  /**
   * Returns `None` if `t` satisfies the predicate `P`, or an error message
   * contained in `Some` otherwise.
   */
  def validated(t: T): Option[String] =
    if (isValid(t)) None else Some(s"Predicate failed: ${show(t)}.")

  /** Checks if `t` does not satisfy the predicate `P`. */
  final def notValid(t: T): Boolean =
    !isValid(t)

  /**
   * Returns the result of [[isValid]] in a `List`. Can be overridden to
   * accumulate the results of sub-predicates.
   */
  def isValidAccumulated(t: T): List[Boolean] =
    List(isValid(t))

  /**
   * Returns the result of [[show]] in a `List`. Can be overridden to
   * accumulate the string representations of sub-predicates.
   */
  def showAccumulated(t: T): List[String] =
    List(show(t))

  private[refined] def contramap[U](f: U => T): Predicate[P, U] =
    new Predicate[P, U] {
      def isValid(u: U): Boolean = self.isValid(f(u))
      def show(u: U): String = self.show(f(u))
      override def validated(u: U): Option[String] = self.validated(f(u))
      override def isValidAccumulated(u: U): List[Boolean] = self.isValidAccumulated(f(u))
      override def showAccumulated(u: U): List[String] = self.showAccumulated(f(u))
    }
}

object Predicate {
  def apply[P, T](implicit p: Predicate[P, T]): Predicate[P, T] = p

  /** Constructs a [[Predicate]] from its parameters. */
  def instance[P, T](isValidF: T => Boolean, showF: T => String): Predicate[P, T] =
    new Predicate[P, T] {
      def isValid(t: T): Boolean = isValidF(t)
      def show(t: T): String = showF(t)
    }

  /** Returns a [[Predicate]] that ignores its inputs and always yields `true`. */
  def alwaysTrue[P, T]: Predicate[P, T] =
    Predicate.instance(_ => true, _ => "true")

  /** Returns a [[Predicate]] that ignores its inputs and always yields `false`. */
  def alwaysFalse[P, T]: Predicate[P, T] =
    Predicate.instance(_ => false, _ => "false")
}