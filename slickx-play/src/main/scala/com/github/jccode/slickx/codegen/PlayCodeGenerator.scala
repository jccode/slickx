package com.github.jccode.slickx.codegen

import slick.model.Model
import StringExt.stringExt

/**
  * PlayCodeGenerator
  *
  * @author 01372461
  */
class PlayCodeGenerator(model: Model) extends CodeGenerator(model) {
  override val customImports: String =
    super.customImports +
    """
      |import com.github.jccode.slickx.play.JsonFormatImplicits._
      |import play.api.libs.json._
    """.stripMargin

  override def Table = new Table(_) {
    table =>
    override def EntityType = new EntityType {
      override def parents: Seq[String] = Seq("BaseEntity")

      override def code: String =
        super.code + "\n" +
        s"""object $name { implicit val ${name.uncapitalize}Format: OFormat[$name] = Json.format[$name]}"""
    }

    override def TableClass = new TableClassDef {
      override def parents: Seq[String] = Seq("BaseTable")
    }

    override def TableValue = new TableValueDef {
      override def rawName: String = model.name.table.toCamelCase.uncapitalize.toPlural
    }

    override def factory: String = if(columns.size == 1) TableClass.elementType else s"(${TableClass.elementType}.apply _).tupled"
  }
}
