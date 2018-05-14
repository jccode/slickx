package com.github.jccode.slickx.play

import java.sql.Timestamp
import java.text.SimpleDateFormat

import play.api.libs.json.{Format, JsString, JsSuccess, JsValue}


trait JsonFormatImplicits {

  implicit object timestampFormat extends Format[Timestamp] {
    val format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SS'Z'")
    def reads(json: JsValue) = {
      val str = json.as[String]
      JsSuccess(new Timestamp(format.parse(str).getTime))
    }
    def writes(ts: Timestamp) = JsString(format.format(ts))
  }

}

object JsonFormatImplicits extends JsonFormatImplicits
