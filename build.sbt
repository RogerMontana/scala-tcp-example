name := "test-task"

version := "1.0"

scalaVersion := "2.12.2"

libraryDependencies +=
  "com.typesafe.akka" %% "akka-stream" % "2.5.1"


PB.targets in Compile := Seq(
  scalapb.gen() -> (sourceManaged in Compile).value
)