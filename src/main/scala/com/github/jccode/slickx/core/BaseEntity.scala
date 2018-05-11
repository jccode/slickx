package com.github.jccode.slickx.core

import java.sql.Timestamp

import shapeless._

trait BaseEntity {
  def id: Int
  def createTime: Timestamp
  def updateTime: Timestamp
}

object BaseEntity {
  val witCreateTime = Witness('createTime)
  val witUpdateTime = Witness('updateTime)
  type TypeCreateTime = witCreateTime.T
  type TypeUpdateTime = witUpdateTime.T

  implicit class WithCreateTime[T <: BaseEntity](t: T) {
    def withCreateTime(time: Timestamp)(implicit mkLens: MkFieldLens.Aux[T, TypeCreateTime, Timestamp]): T = mkLens().set(t)(time)
  }
  implicit class WithUpdateTime[T <: BaseEntity](t: T) {
    def withUpdateTime(time: Timestamp)(implicit mkLens: MkFieldLens.Aux[T, TypeUpdateTime, Timestamp]): T = mkLens().set(t)(time)
  }
}

