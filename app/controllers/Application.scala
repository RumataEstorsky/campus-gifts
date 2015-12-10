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
      "email" -> email.verifying( s"""Такой адрес электронной почты уже подписан!""", email => !Users.getExistingEmails.contains(email)),
      //.verifying("???", email => s"sdkjdfsk $email xdfksjfsdklj" == ""),
      "name" -> text
    )(User.apply)(User.unapply)
  )


  /** Main page (out form email subscribing) */
  def index = DBAction { implicit rs =>
    Ok(indexPage(userForm, Users.getExistingUsers))
  }

  /** Processing "email adding form" */
  def addEmail = DBAction { implicit rs =>
    userForm.bindFromRequest.fold(
      formWithErrors => BadRequest(indexPage(formWithErrors, Users.getExistingUsers)),
      user => {
        Users.insert(user)
        //Home.flashing("success" -> "Computer %s has been created".format(computer.name))
        Home
      }
    )
  }

  /** Action shuffle emails and decides presenter pairs */
  def result = DBAction { implicit rs =>
    val existingEmails = Users.getExistingEmails
    Sender.sendMailToAllCurrentUsers(existingEmails)
    Redirect(routes.Application.index())
      .flashing("result" -> "Все письма были успешно разосланы, теперь можете нажать кнопку очистить список!")
  }


  def clear = DBAction { implicit rs =>
    Users.clear
    Home
  }

  def remove(email: String) = DBAction { implicit rs =>
    Users.remove(email)
    Home
  }

}