package argonaut.playground

import argonaut._, Argonaut._
import javax.servlet.http.{HttpServletRequest, HttpServletResponse}
import scalaz._, Scalaz._
import unfiltered.request._
import unfiltered.response._

/* server state */

case class State(refs: List[Ref], next: Int) {
  def addRef(req: Request, data: Json): State =
    copy(refs = Ref(Ball(next, data), req) :: refs, next = next + 1)

  def takeRef(id: Int): (State, Option[Ref]) =
    (copy(refs = refs.filter(_.ball.id != id)), refs.find(_.ball.id == id))
}


/* something being juggled */

case class Ball(id: Int, data: Json)

object Ball {
  implicit def BallCodecJson: CodecJson[Ball] =
    casecodec2(Ball.apply, Ball.unapply)("id", "data")
}

case class Ref(ball: Ball, req: Request)

/* Some errors that can happen */

sealed trait Error
case class ErrorMessage(message: String) extends Error
case class ErrorDecode(message: String, history: CursorHistory) extends Error
case class ErrorThrown(t: Throwable) extends Error

object Error {
  implicit def ErrorEncodeJson: EncodeJson[Error] =
    EncodeJson({
      case ErrorMessage(message) => Json("error" := message)
      case ErrorDecode(message, history) => Json("error" := s"Message[$message]\nHistory[\n${history.show}\n]")
      case ErrorThrown(t) => Json("error" := s"${t.getClass.getSimpleName}[${t.getMessage}]")
    })

  implicit def ErrorDecodeJson: DecodeJson[Error] =
    DecodeJson(c => for {
      m <- (c --\ "error").as[String]
    } yield ErrorMessage(m))
}
