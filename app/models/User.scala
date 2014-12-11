package models

import play.api.db.slick.Config.driver.simple._
import scala.slick.jdbc.{GetResult, StaticQuery => Q}
import Q.interpolation
import play.api.Play.current
import play.api.db.slick.DB

case class User(email: String, name: String)

class UsersTable(tag: Tag) extends Table[User](tag, "users") {

  def email = column[String]("email", O.PrimaryKey, O.NotNull)
  def name = column[String]("name", O.NotNull)

  def * = (email, name) <> (User.tupled, User.unapply _)


}

object Users {

  val users = TableQuery[UsersTable]

  def count(implicit s: Session): Int = sql"SELECT COUNT(*) FROM users".as[Int].first

  def getExistingEmails = {
    DB.withSession { implicit session =>
      users.map(u => u.email).list
    }
  }

  //TODO direct insert from controller
  // TODO use User.tupled, User.unapply _)
  def insert(email: String, name: String)(implicit s: Session) = users += User(email, name)


}
