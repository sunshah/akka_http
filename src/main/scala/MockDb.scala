import akka.Done

import scala.concurrent.ExecutionContext
import scala.concurrent.Future

final case class Item(name: String, id: Long)
final case class Order(items: List[Item])

class MockDb {

  var orders: List[Item] = Nil

  /** Async DB call using provided execution context */
  def getItem(id: Long)(implicit e: ExecutionContext): Future[Option[Item]] =
    Future {
      /* returns option of an item */
      orders find (_.id == id)
    }

  /** Async DB call to save an order */
  def saveOrder(order: Order)(implicit e: ExecutionContext): Future[Done] = {
    orders = order match {
        /** append to current list of items for order if valid */
      case Order(items) => items ::: orders
      case _            => orders
    }

    Future {Done}
  }

  def getAllOrders()(implicit ec: ExecutionContext): Future[List[Item]] = Future {
    orders
  }

}
