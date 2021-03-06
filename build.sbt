
val slickVersion = "3.2.1"

lazy val shareSettings =Seq(
  organization := "com.github.jccode",
  scalaVersion := "2.12.6",
)


lazy val root = (project in file("."))
  .settings(shareSettings)
  .settings(
    name := "slickx",
    version := "0.1"
  )
  .settings(
    libraryDependencies ++= Seq(
      "com.typesafe.slick" %% "slick" % slickVersion,
      "com.typesafe.slick" %% "slick-codegen" % slickVersion,
      "com.typesafe.slick" %% "slick-hikaricp" % slickVersion,
      "com.chuusai" %% "shapeless" % "2.3.3",
    ))


lazy val `slickx-example` = (project in file("slickx-example"))
  .settings(shareSettings)
  .settings(
    name := "slickx-example",
    version := "0.1"
  )
  .settings(
    libraryDependencies ++= Seq(
      "com.github.jccode" %% "slickx" % "0.1",
      "com.h2database" % "h2" % "1.4.196",
      "org.slf4j" % "slf4j-nop" % "1.7.10",
    )
  )
  .settings(slick := slickCodeGenTask.value)
  .settings(sourceGenerators in Compile += slick)
  .dependsOn(root)



lazy val slick = taskKey[Seq[File]]("gen-tables")  // register manual sbt command

// Define slick code gen task implemention
lazy val slickCodeGenTask = Def.task {
  val (dir, cp, r, s) = ((sourceManaged in Compile).value, (dependencyClasspath in Compile).value, (runner in Compile).value, streams.value)
  val pkg = "dao"
  val slickProfile = "slick.jdbc.H2Profile"
  val jdbcDriver = "org.h2.Driver"
  val url = "jdbc:h2:mem:sample;INIT=RUNSCRIPT FROM 'slickx-example/src/main/resources/sql/drop-tables.sql'\\;RUNSCRIPT FROM 'slickx-example/src/main/resources/sql/create-tables.sql';"
  val user = "sa"
  val password = ""
  val included = ""
  val excluded = ""
  r.run("com.github.jccode.slickx.codegen.CodeGenerator", cp.files, Array(slickProfile, jdbcDriver, url, dir.getPath, pkg, user, password, "true", "com.github.jccode.slickx.codegen.CodeGenerator", included, excluded), s.log)
  val outputFile = dir / pkg.replace(".", "/") / "Tables.scala"
  Seq(outputFile)
}
