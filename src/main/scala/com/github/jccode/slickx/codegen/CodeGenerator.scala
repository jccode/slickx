package com.github.jccode.slickx.codegen

import java.net.URI

import slick.basic.DatabaseConfig
import slick.codegen.SourceCodeGenerator
import slick.jdbc.JdbcProfile
import slick.model.Model

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContext}
import scala.concurrent.ExecutionContext.Implicits.global

import slick.util.ConfigExtensionMethods.configExtensionMethods
import StringExt.stringExt

/**
  * CodeGenerator
  *
  * @author 01372461
  */
class CodeGenerator(model: Model) extends SourceCodeGenerator(model) {

  def customImports = "import com.github.jccode.slickx.core._\n"

  override def entityName: String => String = (dbName: String) => dbName.toCamelCase

  override def tableName: String => String = (dbName: String) => dbName.toCamelCase + "Table"

  override def code: String = customImports+super.code

  override def Table = new Table(_) {
    table =>
    override def EntityType = new EntityType {
      override def parents: Seq[String] = Seq("BaseEntity")
    }

    override def TableClass = new TableClassDef {
      override def parents: Seq[String] = Seq("BaseTable")
    }

    override def TableValue = new TableValueDef {
      override def rawName: String = model.name.table.toCamelCase.uncapitalize.toPlural
    }
  }

}



object CodeGenerator {
  def run(profile: String, jdbcDriver: String, url: String, outputDir: String, pkg: String, user: Option[String], password: Option[String], ignoreInvalidDefaults: Boolean): Unit =
    run(profile, jdbcDriver, url, outputDir, pkg, user, password, ignoreInvalidDefaults, None, None, None)

  def run(profile: String, jdbcDriver: String, url: String, outputDir: String, pkg: String, user: Option[String], password: Option[String], ignoreInvalidDefaults: Boolean, codeGeneratorClass: Option[String],
          included: Option[String], excluded: Option[String]): Unit = {
    val profileInstance: JdbcProfile =
      Class.forName(profile + "$").getField("MODULE$").get(null).asInstanceOf[JdbcProfile]
    val dbFactory = profileInstance.api.Database
    val db = dbFactory.forURL(url, driver = jdbcDriver,
      user = user.getOrElse(null), password = password.getOrElse(null), keepAliveConnection = true)

    val pat = ",(\\W*)|;(\\W*)"
    val includedTables = included.map(_.split(pat))
    val excludedTables = excluded.map(_.split(pat))

    val tables = profileInstance.defaultTables
      .map(ts => ts.filter(t => includedTables.isEmpty || (includedTables.get contains t.name.name)))
      .map(ts => ts.filterNot(t => excludedTables.isDefined && (excludedTables.get contains t.name.name)))

    try {
      val m = Await.result(db.run(profileInstance.createModel(Some(tables), ignoreInvalidDefaults)(ExecutionContext.global).withPinnedSession), Duration.Inf)
      val codeGenerator = codeGeneratorClass.getOrElse("slick.codegen.SourceCodeGenerator")
      val sourceGeneratorClass = Class.forName(codeGenerator).asInstanceOf[Class[_ <: SourceCodeGenerator]]
      val generatorInstance = sourceGeneratorClass.getConstructor(classOf[Model]).newInstance(m)
      generatorInstance.writeToFile(profile,outputDir, pkg)
    } finally db.close
  }

  def run(uri: URI, outputDir: Option[String], ignoreInvalidDefaults: Boolean = true): Unit = {
    val dc = DatabaseConfig.forURI[JdbcProfile](uri)
    val pkg = dc.config.getString("codegen.package")
    val out = outputDir.getOrElse(dc.config.getStringOr("codegen.outputDir", "."))
    val profile = if(dc.profileIsObject) dc.profileName else "new " + dc.profileName
    try {
      val m = Await.result(dc.db.run(dc.profile.createModel(None, ignoreInvalidDefaults)(ExecutionContext.global).withPinnedSession), Duration.Inf)
      new SourceCodeGenerator(m).writeToFile(profile, out, pkg)
    } finally dc.db.close
  }

  def main(args: Array[String]): Unit = {
    args.toList match {
      case uri :: Nil =>
        run(new URI(uri), None)
      case uri :: outputDir :: Nil =>
        run(new URI(uri), Some(outputDir))
      case profile :: jdbcDriver :: url :: outputDir :: pkg :: Nil =>
        run(profile, jdbcDriver, url, outputDir, pkg, None, None, true, None, None, None)
      case profile :: jdbcDriver :: url :: outputDir :: pkg :: user :: password :: Nil =>
        run(profile, jdbcDriver, url, outputDir, pkg, Some(user), Some(password), true, None, None, None)
      case  profile:: jdbcDriver :: url :: outputDir :: pkg :: user :: password :: ignoreInvalidDefaults :: Nil =>
        run(profile, jdbcDriver, url, outputDir, pkg, Some(user), Some(password), ignoreInvalidDefaults.toBoolean, None, None, None)
      case  profile:: jdbcDriver :: url :: outputDir :: pkg :: user :: password :: ignoreInvalidDefaults :: codeGeneratorClass :: Nil =>
        run(profile, jdbcDriver, url, outputDir, pkg, Some(user), Some(password), ignoreInvalidDefaults.toBoolean, Some(codeGeneratorClass), None, None)
      case  profile:: jdbcDriver :: url :: outputDir :: pkg :: user :: password :: ignoreInvalidDefaults :: codeGeneratorClass :: included :: excluded :: Nil =>
        run(profile, jdbcDriver, url, outputDir, pkg, Some(user), Some(password), ignoreInvalidDefaults.toBoolean, Some(codeGeneratorClass), included.toOpt, excluded.toOpt)
      case _ => {
        println("""
                  |Usage:
                  |  SourceCodeGenerator configURI [outputDir]
                  |  SourceCodeGenerator profile jdbcDriver url outputDir pkg [user password]
                  |  SourceCodeGenerator profile jdbcDriver url outputDir pkg user password ignoreInvalidDefaults codeGeneratorClass [included excluded]
                  |
                  |Options:
                  |  configURI: A URL pointing to a standard database config file (a fragment is
                  |    resolved as a path in the config), or just a fragment used as a path in
                  |    application.conf on the class path
                  |  profile: Fully qualified name of Slick profile class, e.g. "slick.jdbc.H2Profile"
                  |  jdbcDriver: Fully qualified name of jdbc driver class, e.g. "org.h2.Driver"
                  |  url: JDBC URL, e.g. "jdbc:postgresql://localhost/test"
                  |  outputDir: Place where the package folder structure should be put
                  |  pkg: Scala package the generated code should be places in
                  |  user: database connection user name
                  |  password: database connection password
                  |
                  |When using a config file, in addition to the standard config parameters from
                  |slick.basic.DatabaseConfig you can set "codegen.package" and
                  |"codegen.outputDir". The latter can be overridden on the command line.
                """.stripMargin.trim)
        System.exit(1)
      }
    }
  }
}