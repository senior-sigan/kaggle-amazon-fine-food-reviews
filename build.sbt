lazy val commonSettings = Seq(
  organization := "it.sevenbits",
  name := "amazon-fine-foods",
  version := "1.0",
  scalaVersion := "2.11.8"
)

lazy val parser = project.in(file("./parser"))
  .settings(commonSettings: _*)
  .settings(libraryDependencies ++= Seq(
    "org.apache.spark" %% "spark-core" % "2.0.1" % "provided",
    "org.apache.spark" %% "spark-sql" % "2.0.1" % "provided",
    "net.sf.opencsv" % "opencsv" % "2.0",
    "com.rabbitmq" % "amqp-client" % "3.6.5"
  ))

lazy val translator = project.in(file("./translator"))
  .settings(commonSettings: _*)
  .settings(libraryDependencies ++= Seq(
    "com.rabbitmq" % "amqp-client" % "3.6.5",
    "com.typesafe.akka" %% "akka-actor" % "2.4.11",
    "com.typesafe.akka" %% "akka-http-core" % "2.4.11"
  ))

lazy val api = project.in(file("./api"))
  .settings(commonSettings: _*)
  .settings(libraryDependencies ++= Seq(
    "com.typesafe.akka" %% "akka-actor" % "2.4.11",
    "io.spray" %% "spray-can" % "1.3.3",
    "io.spray" %% "spray-routing" % "1.3.3"
  ))