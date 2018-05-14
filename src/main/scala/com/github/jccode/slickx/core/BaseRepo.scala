package com.github.jccode.slickx.core

import java.sql.Timestamp

import com.github.jccode.slickx.core.BaseEntity.{TypeCreateTime, TypeUpdateTime}
import shapeless.MkFieldLens
import slick.basic.DatabaseConfig
import slick.jdbc.JdbcProfile
import slick.lifted.TableQuery

import scala.concurrent.Future

/**
  * BaseRepo
  *
  * @author 01372461
  */
//trait BaseRepo[T <: slick.lifted.AbstractTable[_], Q <: TableQuery[T]] {
//
//  def all(): Future[Seq[T#TableElementType]]
//
//  def get(id: Int): Future[Option[T#TableElementType]]
//
//  def insert(entity: T#TableElementType): Future[Int]
//
//  def update(entity: T#TableElementType): Future[Int]
//
//  def delete(id: Int): Future[Int]
//
//}

trait BaseRepo[E <: BaseEntity, T <: slick.lifted.AbstractTable[E], Q <: TableQuery[T]] {

  def all(): Future[Seq[E]]

  def get(id: Int): Future[Option[E]]

  def insert(entity: E): Future[Int]

  def update(entity: E): Future[Int]

  def delete(id: Int): Future[Int]

}


// (implicit ev: Q =:= TableQuery[T])
class AbstractRepo[P <: JdbcProfile, E <: BaseEntity, T <: P#Table[E] with BaseTable, Q <: TableQuery[T]]
(val dbConfig: DatabaseConfig[P], val elements: Q)
(implicit createTimeLens: MkFieldLens.Aux[E, TypeCreateTime, Timestamp], updateTimeLens: MkFieldLens.Aux[E, TypeUpdateTime, Timestamp],
 ev: Q =:= TableQuery[T], ev2: E =:= T#TableElementType)
  extends BaseRepo[E, T, Q] {

  import dbConfig.profile.api._

  protected val db = dbConfig.db

  protected implicit def action2Future[T](action: DBIO[T]): Future[T] = db.run(action)

  protected def byId(id: Int) = elements.filter(_.id === id)

  private def returnId = elements returning elements.map(_.id)

  override def all(): Future[Seq[E]] = db.run(elements.result)

  override def get(id: Int): Future[Option[E]] = db.run(byId(id).result.headOption)

  override def insert(entity: E): Future[Int] = db.run(returnId += beforeInsert(entity))

  override def update(entity: E): Future[Int] = db.run(byId(entity.id).update(beforeUpdate(entity)))

  override def delete(id: Int): Future[Int] = db.run(byId(id).delete)


  private def now = new Timestamp(System.currentTimeMillis())

  protected def beforeUpdate(entity: E): E = entity.withUpdateTime(now)

  protected def beforeInsert(entity: E): E = entity.withCreateTime(now).withUpdateTime(now)

}

