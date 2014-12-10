package controllers


import java.io.{PrintWriter, File}
import play.api.Play
import play.api.mvc._
import views.html._
import io.Source
import scala.util.Random
import play.api.Play.current

object Application extends Controller {
  val emailsFile = "emails.txt"

  /** Main page (out form email subscribing) */
  def index = Action {
    Ok(indexPage("", "", getExistingEmails))
  }

  /** Processing "email adding form" */
  def addEmail = Action { implicit request =>

    val email = request.body.asFormUrlEncoded.get("email").mkString.toLowerCase.trim
    val existingEmails = getExistingEmails

    val error = if(email.isEmpty) "Нужно ввести адрес электронной почты"
                else if(existingEmails.contains(email)) s"""Адрес электронной почты "$email" уже подписан!"""
                else ""

    if(error.isEmpty) {
      saveEmails(getExistingEmails ::: List(email))
    }

    Ok(indexPage(email, error, getExistingEmails))
  }

  /** Action shuffle emails and decides presenter pairs */
  def result = Action {
    val existingEmails = getExistingEmails
    Ok(resultPage(existingEmails, calculateOrder(existingEmails)))
  }

  def sendMails() = Action {
    val existingEmails = getExistingEmails
    calculateOrder(existingEmails).sliding(2).foreach { case Seq(from, to) =>
      val content = letter(existingEmails(from), existingEmails(to)).toString()
      sendMail(existingEmails(from), content)
    }
    Ok("Письма разосланы")
  }


  def sendMail(to: String, content: String) {
    import com.typesafe.plugin._
    val mail = use[MailerPlugin].email
    mail.setSubject("Поздравь друга!")
    //получатель mail.setRecipient("Peter Hausel Junior <noreply@email.com>","example@foo.com")
    mail.setRecipient(to)
    mail.setFrom(Play.current.configuration.getString("smtp.from").get)
    mail.send(content)
  }

  def calculateOrder(existingEmails: List[String]) = {
    val randomNumbers = generateRandomNumbers(existingEmails.length, Seq())
    if(randomNumbers.size % 2 == 0) randomNumbers else randomNumbers :+ randomNumbers.head
  }

  /** load emails from file */
  def getExistingEmails =
    if((new File(emailsFile)).exists) Source.fromFile(emailsFile).getLines().toList
    else List()

  /** save emails to file */
  def saveEmails(emails: List[String]) = {
    val out = new PrintWriter(emailsFile)
    out.write(emails.mkString("\n"))
    out.close()
  }

  /** generate random numbers from 0 to n, without duplicates */
  def generateRandomNumbers(n: Int, numbers: Seq[Int]): Seq[Int] = {
    def add(x: Int) = if(numbers.contains(x)) numbers else numbers :+ x
    if (numbers.length == n) numbers
    else generateRandomNumbers(n, add(Random.nextInt(n)))
  }


}