import akka.Done
import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.stream.ActorMaterializer
import spray.json.DefaultJsonProtocol._

import scala.concurrent.Future
import scala.io.StdIn

object WebServer {

  /* Actors are objects that encapsulate state and behavior, they communicate exclusively by exchanging messages which
   * are placed on the recipients mailbox
   *
   * ActorSystem is a heavyweight structure which will allocate 1...N threads. Create one per logical application
   */
  implicit val system = ActorSystem()
  /* Materialization is the process of allocating resources needed to run the computation
   * This could mean starting up actors which powers processing or opening files/sockets etc
   */
  implicit val materializer = ActorMaterializer()
  /*Execution context required for future threadpool
   */
  implicit val executionContext = system.dispatcher

  /* Formats for marshalling and unmarshalling on/from the wire
   * jsonFormat2 = case class with 2 parameters
   * jsonFormat1 = case class with 1 parameter
   */
  implicit val itemFormat = jsonFormat2(Item)
  implicit val orderFormat = jsonFormat1(Order)

  val db = new MockDb

  def main(args: Array[String]): Unit = {

    /* Route is an alias for function which turns a RequestContext into a Future[RouteResult]
     * When a route receives a request (RequestContext) it can:
     *  complete request by returning the value of requestContext.complete(...)
     *  reject the request by returning the value of requestContext.reject(...)
     *  fail the request by returning the value of requestContext.fail(...) or by throwing an Exception
     *  do any kind of asynchronous processing and instantly return a Future[RouteResult] to be completed later
     *
     * RequestContext wraps the HttpRequest instance to include information required by the routing logic like
     *  ExecutionContext, Materializer, LoggingAdapter and configured RoutingSetting
     *
     * It also contains unmatchedPath, a value that describes how much of the request URI has not been matched by a
     * PathDirective
     *
     * You form a routing structure/tree by combining directives and custom routes via nesting and the ~ operator.
     * When a request arrives, it is injected into the root of this tree and flows down through all the branches in a
     * DFS manner until either some node completes it or is fully rejected
     *
     * val route =
     * a {
     *  b {
     *    c {
     *      ... // route 1
     *    } ~
     *    d {
     *      ... // route 2
     *    } ~
     *    ... // route 3
     *  } ~
     *  e {
     *    ... // route 4
     *  }
     * }
     *
     * Here five directives form a routing tree.
     *  Route 1 will only be reached if directives a, b and c all let the request pass through.
     *  Route 2 will run if a and b pass, c rejects and d passes.
     *  Route 3 will run if a and b pass, but c and d reject.
     */
    val route: Route =

      pathPrefix("item" / LongNumber) { id =>
        get {
          // there might be no item for a given id
          val maybeItem: Future[Option[Item]] = db.getItem(id)

          onSuccess(maybeItem) {
            case Some(item) => complete(item)
            case None       => complete(StatusCodes.NotFound)
          }
        }

      } ~
        path("order") {
          post {
            entity(as[Order]) { order =>
              val saved: Future[Done] = db.saveOrder(order)
              onComplete(saved) { done =>
                complete("order created")
              }
            }
          } ~
            get {
              val orderItemsFuture = db.getAllOrders()
              onComplete(orderItemsFuture) { items =>
                complete(items)
              }
            }
        } ~
    pathSingleSlash {
      complete("Server up and running")
    }



    /*Bind routes defined to localhost and port
     *  Needs materializer to have been initialized.
     */
    val bindingFuture = Http().bindAndHandle(route, "localhost", 8080)
    println(
      s"Server online at http://localhost:8080/\n Press RETURN to stop...")
    StdIn.readLine() // let it run until user presses enter
    bindingFuture
      .flatMap(_.unbind()) // unbind from the port
      .onComplete(_ => system.terminate()) // shutdown system

  }
}
