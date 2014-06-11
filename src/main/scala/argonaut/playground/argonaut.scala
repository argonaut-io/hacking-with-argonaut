package argonaut.playground

object JsonRequest {
  import unfiltered.request._
  import argonaut._, Argonaut._

  def apply[T](r: HttpRequest[T]) =
    new ParseWrap(r, new Parse[HttpRequest[T]] {
      def parse(req: HttpRequest[T]) = JsonParser.parse(Body.string(req))
    })
}


object JsonResponse {
  import unfiltered.response._
  import argonaut._, Argonaut._

  def apply[A: EncodeJson](a: A) =
    JsonContent ~> ResponseString(a.jencode.spaces2)
}
