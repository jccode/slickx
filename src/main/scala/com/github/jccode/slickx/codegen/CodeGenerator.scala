package com.github.jccode.slickx.codegen

import slick.codegen.SourceCodeGenerator
import slick.model.Model

/**
  * CodeGenerator
  *
  * @author 01372461
  */
class CodeGenerator(model: Model) extends SourceCodeGenerator(model) {

  val customImports = "import com.github.jccode.slickx.core._\n"

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

  implicit class StringExt(val s: String) {
    def toPlural: String = if(s.matches(".*(x|ch|ss|sh|o)$")) { s"${s}es" } else { s"${s}s" }
  }
}
