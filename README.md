# Introduction

Akka HTTP is a lightweight implementation of a full HTTP server and client stacks on top of *akka-actor* and
*akka-stream*. It is ideal for providing itegration between RESTful services as opposed to having a full
framework. Read more [here](https://doc.akka.io/docs/akka-http/current/introduction.html)

This very basic example allows users to create an order made up of items. Here are the class definitions:
```
final case class Item(name: String, id: Long)
final case class Order(items: List[Item])
```
Backend is a basic in-memory DB that holds data for duration of the application

# Usage

Checkout the code and use `sbt` to fire up sbt shell.

Run the server
---
`~run`

The server should be up and running, it defaults to port 8080. Open up a browser and 
enter `http://localhost:8080`

You should see a `Server is up and running` message

Creating and viewing Orders and Items using curl:
---
`GET` root 

`curl -vv http://localhost:8080/`

`POST` list of items to be added to the order

`curl -vv -X POST -H 'Content-Type: application/json' -d '{"items": [{"name" : "item1", "id" : 1}, {"name": "item2", "id": 2}]}' http://localhost:8080/order`

`GET` list of items in the order

`curl -vv http://localhost:8080/order`

`GET` specific items by id

`curl -vv http://localhost:8080/item/1`
