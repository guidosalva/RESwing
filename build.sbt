name in ThisBuild := "reswing"

organization in ThisBuild := "de.tuda.stg"

version in ThisBuild := "0.0.0"

scalaVersion in ThisBuild := "2.11.2"

libraryDependencies ++= Seq(
  "org.scala-lang" % "scala-swing" % "2.11+",
  "de.tuda.stg" %% "rescala" % "0+"
)
