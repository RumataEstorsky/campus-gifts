package controllers

import scala.util.Random
import models._
import play.api._
import play.api.mvc._
import play.api.data._
import play.api.data.Forms._
import play.api.Play.current
import play.api.db.slick._
import views.html._

object Application extends Controller {
  val Home = Redirect(routes.Application.index)

  val userForm = Form(
    mapping(
      "email" -> email.verifying(s"""Такой адрес электронной почты уже подписан!""", email => !Users.getExistingEmails.contains(email)),
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
        Users.insert(user.email, "Friend")
        //Home.flashing("success" -> "Computer %s has been created".format(computer.name))
        Home
      }
    )
  }

  /** Action shuffle emails and decides presenter pairs */
  def result = DBAction { implicit rs =>
    val existingEmails = Users.getExistingEmails
    Ok(resultPage(existingEmails, calculateOrder(existingEmails)))
  }

  def sendMails() = Action {
    val existingEmails = Users.getExistingEmails
    calculateOrder(existingEmails).sliding(2).foreach { case Seq(from, to) =>
      val content = letter(existingEmails(from), existingEmails(to)).toString()
      sendMail(existingEmails(from), content)
    }
    Ok("Письма разосланы")
  }

  def clear = DBAction { implicit rs =>
    Users.clear
    Home
  }

  def remove(email: String) = DBAction { implicit rs =>
    Users.remove(email)
    Home
  }


    def sendMail(to: String, content: String) {
    import com.typesafe.plugin._
    val mail = use[MailerPlugin].email
    mail.setSubject("Поздравь друга!")
    mail.setRecipient(to)
    mail.setFrom(Play.current.configuration.getString("smtp.from").get)
    mail.send(content)
  }

  def calculateOrder(existingEmails: List[String]) = {
    val randomNumbers = generateRandomNumbers(existingEmails.length, Seq())
    if(randomNumbers.size % 2 == 0) randomNumbers else randomNumbers :+ randomNumbers.head
  }


  /** generate random numbers from 0 to n, without duplicates */
  def generateRandomNumbers(n: Int, numbers: Seq[Int]): Seq[Int] = {
    def add(x: Int) = if(numbers.contains(x)) numbers else numbers :+ x
    if (numbers.length == n) numbers
    else generateRandomNumbers(n, add(Random.nextInt(n)))
  }


}