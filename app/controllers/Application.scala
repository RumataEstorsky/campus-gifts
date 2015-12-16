package controllers

import helpers.Sender
import models._
import play.api.Play.current
import play.api.data.Forms._
import play.api.data._
import play.api.db.slick._
import play.api.mvc._
import views.html._

object Application extends Controller {
  val Home = Redirect(routes.Application.index)


  val userForm = Form(
    mapping(
      "email" -> email,
      "name" -> text,
      "roomLink" -> text
    )(User.apply)(User.unapply).verifying( s"""Такой адрес электронной почты уже подписан!""", fields => fields match {
      case user => Users.getForRoom(user.roomLink).contains(user.email)
    })
  )


  /** Main page (out form email subscribing) */
  def index = DBAction { implicit rs =>
    Ok(indexPage())
  }

  def createRoom = Action {
    Redirect(routes.Application.room(Users.generateRoomLink(13)))
  }

  def room(roomLink: String) = DBAction { implicit rs =>
    Ok(roomPage(roomLink, userForm, Users.getForRoom(roomLink)))
  }


  /** Processing "email adding form" */
  def addEmail(roomLink: String) = DBAction { implicit rs =>
    userForm.bindFromRequest.fold(
      formWithErrors => BadRequest(roomPage(roomLink, formWithErrors, Users.getForRoom(roomLink))),
      user => {
        Users.insert(user)
        Redirect(routes.Application.room(roomLink))
      }
    )
  }

  /** Action shuffle emails and decides presenter pairs */
  def result(roomLink: String) = DBAction { implicit rs =>
    val existingEmails = Users.getForRoom(roomLink)
    Sender.sendMailToAllCurrentUsers(existingEmails)
//    Redirect(routes.Application.index()).flashing("result" -> "Все письма были успешно разосланы, теперь можете нажать кнопку очистить список!")
    Redirect(routes.Application.removeRoom(roomLink))
  }


  def removeRoom(roomLink: String) = DBAction { implicit rs =>
    Users.clearAllInRoom(roomLink)
    Home
  }

  def removeEmail(roomLink: String, email: String) = DBAction { implicit rs =>
    Users.remove(roomLink, email)
    Redirect(routes.Application.room(roomLink))
  }

}