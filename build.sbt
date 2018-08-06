name := "akka_http"

version := "0.1"

scalaVersion := "2.12.6"

lazy val akka_http = "com.typesafe.akka" %% "akka-http" % "10.1.3"
lazy val akka_stream = "com.typesafe.akka" %% "akka-stream" % "2.5.12"
lazy val spray_json = "com.typesafe.akka" %% "akka-http-spray-json" % "10.1.3"

libraryDependencies ++= Seq(akka_http, akka_stream, spray_json)