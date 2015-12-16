package models

import play.api.Play.current
import play.api.db.slick.Config.driver.simple._
import play.api.db.slick.DB

import scala.slick.jdbc.StaticQuery.interpolation
import scala.slick.jdbc.{StaticQuery => Q}
import scala.util.Random

case class User(email: String, name: String, roomLink: String) {
  override def toString: String = s"$name - $email"
}

class UsersTable(tag: Tag) extends Table[User](tag, "users") {

  def email = column[String]("email", O.NotNull)
  def name = column[String]("name", O.NotNull)
  def roomLink = column[String]("room_link", O.NotNull)

  def * = (email, name, roomLink) <> (User.tupled, User.unapply _)
  def uniqRoomEmail = index("uniq_room_email", (roomLink, email), unique = true)

}

object Users {

  val users = TableQuery[UsersTable]

  def count(implicit s: Session): Int = sql"SELECT COUNT(*) FROM users".as[Int].first

  def getForRoom(roomLink: String) = {
    DB.withSession { implicit session =>
      users.filter(u => u.roomLink === roomLink).list
    }
  }

  def clearAllInRoom(roomLink: String)(implicit s: Session) = users.filter(u => u.roomLink === roomLink).delete

  def remove(roomLink: String,email: String)(implicit s: Session) =
    users.filter(u => u.roomLink === roomLink && u.email === email).delete

  def insert(user: User)(implicit s: Session) = users += user

  def generateRoomLink(n: Int) = (1 to n).map{x => (Random.nextInt('z' - 'a') + 'a').toChar}.mkString

}
