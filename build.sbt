name := "slickx"

version := "0.1"

organization := "com.github.jccode"

scalaVersion := "2.12.6"

val slickVersion = "3.2.1"


libraryDependencies ++= Seq(
  "com.typesafe.slick" %% "slick" % slickVersion,
  "com.typesafe.slick" %% "slick-codegen" % slickVersion,
  "com.typesafe.slick" %% "slick-hikaricp" % slickVersion,
  "com.chuusai" %% "shapeless" % "2.3.3",
)
