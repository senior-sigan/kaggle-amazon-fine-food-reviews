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
  .settings(resolvers ++= Seq("Twitter Maven" at "https://maven.twttr.com"))
  .settings(libraryDependencies ++= Seq(
    "com.twitter" %% "finatra-http" % "2.5.0",
    "com.twitter" %% "finatra-slf4j" % "2.5.0",
    "ch.qos.logback" % "logback-classic" % "1.1.3"
  ))