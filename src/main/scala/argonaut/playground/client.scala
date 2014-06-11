package argonaut.playground

import argonaut._, Argonaut._
import dispatch._, Defaults._
import scalaz._, Scalaz._

case class Client(base: String) {
  /* list of requests currently being juggled */
  def list: Error \/ List[Ball] =
    run { dispatch.url(s"${base}/juggle").GET }

  /* submit a request to be juggled */
  def submit[A: EncodeJson](a: A): Error \/ Json =
    run { dispatch.url(s"${base}/juggle").PUT.setBody(a.asJson.spaces2).setContentType("application/json", "utf-8") }

  /* see if a specific request exists */
  def get(id: Int): Error \/ Ball =
    run { dispatch.url(s"${base}/juggle/${id}").GET }

  /* respond to a specific request */
  def respond(id: Int, json: Json): Error \/ Unit =
    run { dispatch.url(s"${base}/juggle/${id}").POST.setBody(json.spaces2).setContentType("application/json", "utf-8") }

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
