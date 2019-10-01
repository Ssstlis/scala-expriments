import scala.reflect.ClassTag

import ReComposeTest._
import cats.Id
import cats.data.Kleisli
import shapeless.ops.tuple.{Length, Prepend, Split}
import shapeless.{Nat, Succ, _0}
import shapeless.syntax.std.tuple._
import shapeless.syntax.nat._

object Main {
  def main(args: Array[String]): Unit = {

    val a: String => String => Int = f1 => f2 => (f1.toInt + 1) * (f2.toInt + 1)
    val b: String => Int => String = f1 => f2 => ((f1.toInt + 1) * f2).toString
    val c1: Long => String => Boolean = _ => _ => true
    val d1: Boolean => Boolean => Char = _ => _ => 'r'

    val c: String => Kleisli[Id, Int, String] = _ => Kleisli[Id, Int, String](_ => "123")
    val d: String => Kleisli[Id, String, Boolean] = _ => Kleisli[Id, String, Boolean](_ => true)
    val e: String => Kleisli[Id, Boolean, Long] = _ => Kleisli[Id, Boolean, Long](_ => 0L)
    val f: String => Kleisli[Id, Long, String] = _ => Kleisli[Id, Long, String](_ => "1234")
    import ReCompose._

    a.pairAndThen(b)
    /*
        class Testing2[T, T1, R <: Nat, P](value1: T, value2: T1, L1: R, P: P) {
          def apply[P](implicit Prep: Prepend.Aux[T, T1, P]): P = {
            Prep.apply(value1, value2)
          }
        }*/

    class Testing1[T, T1, R <: Nat](value1: T, value2: T1, L1: R) {
      def apply[P, Out](implicit Prep: Prepend.Aux[T, T1, P], S: Split.Aux[P, R, Out]): Out = {
        S(Prep(value1, value2))
      }
    }

    def testing1[T, T1, R <: Nat](value1: T, value2: T1)(implicit L1: Length.Aux[T, R]): Testing1[T, T1, R] = {
      new Testing1(value1, value2, L1(value1))
    }

    class toReComposeOps1[A <: Product, B, C](f: A => B => C) {
      def pairAndThen[A1, D, L <: Nat, P](g: A1 => C => D)(
        implicit
        L1: Length.Aux[A, L],
        P: Prepend.Aux[A, A1, P],
        S: Split[P, L],
        C1: ClassTag[A],
        C2: ClassTag[A1]
      ): P => B => D = {
        p => S(p) match {
          case (a: A, a1: A1) => f(a) andThen g(a1)
        }
      }
      def >++> : Int = 3
      def >+:> : Int = 3
      def >:+> : Int = 3
    }

    def toReCo[A <: Product, B, C](f: A => B => C): toReComposeOps1[A, B, C] = {
      new toReComposeOps1[A, B, C](f)
    }

    class toReComposeOps2[X, A <: Tuple1[X], B, C](f: A => B => C) {
      def pairAndThen[A1, D, L <: Nat, P](
        g: A1 => C => D
      )(
        implicit
        L1: Length.Aux[A, L],
        P: Prepend.Aux[A, A1, P],
        S: Split[P, L],
        C1: ClassTag[A],
        C2: ClassTag[A1]
      ): P => B => D = {
        p => S(p) match {
          case (a: A, a1: A1) => f(a) andThen g(a1)
        }
      }
    }

    class toReComposeOps3[A, B, C](f: A => B => C) {
      def pairAndThen[X, A1 <: Tuple1[X], D, L <: Nat, P](
        g: A1 => C => D
      )(
        implicit
        L1: Length.Aux[A, L],
        P: Prepend.Aux[A, A1, P],
        S: Split[P, L],
        C1: ClassTag[A],
        C2: ClassTag[A1]
      ): P => B => D = {
        p => S(p) match {
          case (a: A, a1: X) => f(a) andThen g(a1)
        }
      }
    }

    testing1(("", 1, 5), ("", 1, 5)).apply

    val ttt = ("", 1, 5) ::: ("", 1, 5)
    val test = ("", 1, 5).length
    ttt.split(test) match {
      case (tuple, tuple1) =>
    }

    Tuple1("!23") ::: Tuple1(4)
    new toReComposeOps1[Tuple2[String, String], String, String](
      new toReComposeOps1[Tuple1[String], String, Int](a.compose((_: Tuple1[String])._1)).pairAndThen(b.compose((_: Tuple1[String])._1))
    ).pairAndThen(c1.compose((_: Tuple1[Long])._1))

    new toReComposeOps1[(String, String, Long), String, Boolean](
      new toReComposeOps1[(String, String), String, String](
        new toReComposeOps1[Tuple1[String], String, Int](
          a.compose((_: Tuple1[String])._1)
        ).pairAndThen(b.compose((_: Tuple1[String])._1)
        )).pairAndThen(c1.compose((_: Tuple1[Long])._1))
    ).pairAndThen(d1.compose((_: Tuple1[Boolean])._1))

    toReCo(a.pairAndThen(b)).pairAndThen(c1)
    new toReComposeOps3 /*[(String, String, Long), String, Boolean]*/ (
      new toReComposeOps3 /*[(String, String), String, String]*/ (
        new toReComposeOps3 /*[*//*Tuple1[*//*String*//*]*//*, String, Int]*/ (
          a /*.compose((_: Tuple1[String])._1)*/
        ).pairAndThen(b /*.compose((_: Tuple1[String])._1*/)
      ).pairAndThen(c1 /*.compose((_: Tuple1[Long])._1)*/)
    ).pairAndThen(d1 /*.compose((_: Tuple1[Boolean])._1)*/)

    c.pairAndThen(d).pairAndThen(e).pairAndThen(f)(("", ("", ("", ""))))

    println(a.pairAndThen(b)("1", "2")("3"))

    println(c.pairAndThen(d)("1", "2")(0))
  }
}
