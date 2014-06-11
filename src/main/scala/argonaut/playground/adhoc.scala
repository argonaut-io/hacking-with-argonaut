package argonaut.playground

import argonaut._, Argonaut._
import dispatch._, Defaults._
import scalaz._, Scalaz._

object github {
  def branches: Error \/ Json =
    run { dispatch.url(s"https://api.github.com/repos/argonaut-io/argonaut/branches").GET.secure }

  def run[A: DecodeJson](req: => Req): Error \/ A = {
    val http = new dispatch.Http()
    try http(req).either().disjunction.leftMap(ErrorThrown).flatMap(res =>
      if (res.getStatusCode == 200) decode[A](res) else decode[Error](res).flatMap(_.left[A]))
    finally http.shutdown
  }

  def decode[X: DecodeJson](res: Res): Error \/ X = {
    val body = res.getResponseBody
    body.decodeWith[Error \/ X, X](_.right, e => ErrorMessage(e).left, (e, c) => ErrorDecode(e, c).left)
  }
}
