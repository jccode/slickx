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
trait BaseRepo[E <: BaseEntity] {

  def all(): Future[Seq[E]]

  def get(id: Int): Future[Option[E]]

  def insert(entity: E): Future[Int]

  def update(entity: E): Future[Int]

  def delete(id: Int): Future[Int]

}

class AbstractRepo[P <: JdbcProfile, E <: BaseEntity, T <: P#API#Table[E] with BaseTable]
(val dbConfig: DatabaseConfig[P], val elements: TableQuery[T])
(implicit createTimeLens: MkFieldLens.Aux[E, TypeCreateTime, Timestamp], updateTimeLens: MkFieldLens.Aux[E, TypeUpdateTime, Timestamp])
  extends BaseRepo[E] with SlickExtOpts {

  import dbConfig.profile.api._

  protected val db = dbConfig.db

  protected implicit def action2Future[T](action: DBIO[T]): Future[T] = db.run(action)

  protected def byId(id: Int) = elements.filter(_.id === id)

  private def returnId = elements returning elements.map(_.id)

  private def now = new Timestamp(System.currentTimeMillis())

  protected def beforeUpdate(entity: E): E = entity.withUpdateTime(now)

  protected def beforeInsert(entity: E): E = entity.withCreateTime(now).withUpdateTime(now)

  override def all(): Future[Seq[E]] = db.run(elements.result)

  override def get(id: Int): Future[Option[E]] = db.run(byId(id).result.headOption)

  override def insert(entity: E): Future[Int] = db.run(returnId += beforeInsert(entity))

  override def update(entity: E): Future[Int] = db.run(byId(entity.id).update(beforeUpdate(entity)))

  override def delete(id: Int): Future[Int] = db.run(byId(id).delete)
}
