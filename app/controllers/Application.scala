package controllers


import play.api.mvc._
import views.html._
import io.Source

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
  def shuffle = Action {
    Ok("Перемешаны адреса")
  }

  def getExistingEmails = Source.fromFile(emailsFile).getLines().toList

  def saveEmails(emails: List[String]) = {
    import java.io.PrintWriter
    val out = new PrintWriter(emailsFile)
    out.write(emails.mkString("\n"))
    out.close()
  }

}