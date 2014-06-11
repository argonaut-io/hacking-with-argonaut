package argonaut

import scalaz._, Scalaz._
import javax.servlet.http.{HttpServletRequest, HttpServletResponse}
import unfiltered.request._

package object playground {
  type Request = HttpRequest[HttpServletRequest] with unfiltered.Async.Responder[HttpServletResponse]
  implicit class UnsafeDisjunctionSyntax[A, B](x: A \/ B) {
    def unethical: B =
      x.toOption.get
  }
}
