package argonaut.playground

import argonaut._, Argonaut._
import javax.servlet.http.{HttpServletRequest, HttpServletResponse}
import scalaz._, Scalaz._
import unfiltered.request._
import unfiltered.response._

object server {
  /* list of requests currently being juggled */
  def list: List[Ball] =
    state.get.refs.map(_.ball)

  /* submit a request to be juggled */
  def submit: HttpRequest[HttpServletRequest] with unfiltered.Async.Responder[HttpServletResponse] => Json => Unit =
    req => data => tx { st => st.addRef(req, data).squared }

  /* see if a specific request exists */
  def get: Int => Option[Ball] =
    id => state.get.refs.filter(x => x.ball.id == id).headOption.map(_.ball)

  /* respond to a specific request */
  def respond: Int => Json => Option[Unit] =
    id => data => get(id).flatMap(ref =>
      tx { st => st.takeRef(id) } map (ref =>
        \/.fromTryCatch(ref.req.respond(Ok ~> JsonResponse(data))).toOption))

  val state = new java.util.concurrent.atomic.AtomicReference[State](State(
    refs = Nil,
    next = 0
  ))

  @annotation.tailrec def tx[A](update: State => (State, A)): A = {
    val s1 = state.get
    val (s2, a) = update(s1)
    if (state.compareAndSet(s1, s2)) a else tx(update)
  }

  object AsyncPlan extends unfiltered.filter.async.Plan  {
    override def asyncRequestTimeoutMillis = 1000 * 60 * 30

    def intent = {
      case req @ Path("/juggle") =>
        req match {
          case PUT(_) =>
            withJson[Json](req)(submit(req))
          case GET(_) =>
            req.respond(Ok ~> JsonResponse(list))
          case _ =>
            invalidMethod(req)
        }
      case req @ Path(Seg("juggle" :: AnInt(id) :: Nil)) =>
        req match {
          case GET(_) =>
            req.respond(get(id) match {
              case None =>
                NotFound ~> JsonResponse(ErrorMessage(s"$id is not available"))
              case Some(ball) =>
                Ok ~> JsonResponse(ball)
            })
          case POST(_) =>
            withJson[Json](req)(data => req.respond(respond(id)(data) match {
              case None => NotFound ~> JsonResponse(ErrorMessage(s"$id is not available"))
              case Some(v) => Ok ~> JsonResponse(())
            }))
          case _ =>
            invalidMethod(req)
        }
    }

    def withJson[A: DecodeJson](req: Request)(f: A => Unit) =
      JsonRequest(req).decodeWith[Unit, A](
        f
      , m => req.respond(BadRequest ~> JsonResponse(ErrorMessage(m)))
      , (m, h) => req.respond(BadRequest ~> JsonResponse(ErrorDecode(m, h)))
      )

    def invalidMethod(req: Request) =
      req.respond(MethodNotAllowed ~> JsonResponse(ErrorMessage("Invalid method: " + req.method)))
  }

  def runDefault(): Client =
    run(10080)

  def run(port: Int): Client = {
    unfiltered.jetty.Http.local(port).filter(AsyncPlan).start
    val addr = java.net.InetAddress.getLocalHost.getHostAddress
    Client(s"http://${addr}:${port}")
  }

  def stop(): Unit = {
    println("Sorry if you thought this was going to be elegant.")
    Runtime.getRuntime.halt(0)
  }

  object AnInt {
    def unapply(n: String): Option[Int] =
      n.parseInt.toOption
  }
}
