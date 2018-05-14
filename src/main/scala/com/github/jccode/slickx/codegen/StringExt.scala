package com.github.jccode.slickx.codegen

class StringExt(val s: String) {
  def toPlural: String = if(s.matches(".*(x|ch|ss|sh|o)$")) { s"${s}es" } else { s"${s}s" }

  def toOpt: Option[String] = if(s == null || s.trim.isEmpty) None else Some(s)
}

object StringExt {
  @inline implicit def stringExt(s: String): StringExt = new StringExt(s)
}