package helpers

import models._
import play.api.Play
import play.api.templates.Html
import views.html._
import play.api.Play.current
import scala.util.Random

/**
 * Created by rumata on 10.12.15.
 */
object Sender {
  val testEmail = "valera.dt@gmail.com"

  def sendMailToAllCurrentUsers(existingUsers: List[User]) = {
    val existingEmails = Users.getExistingEmails
    if (existingEmails.nonEmpty) {
      val mixedUsers = calculateOrder(existingUsers)
      val testReport = testReportLetter(mixedUsers)

      sendTestReport(testReport)

      mixedUsers.sliding(2).foreach { case Seq(from, to) =>
        val content = letter(from, to).toString()
        sendMail(from.email, content)
      }

    }
  }

  /** returns mixed users by special gift-algorithm */
  def calculateOrder(existingUsers: List[User]) = {
    val randomNumbers = generateRandomNumbers(existingUsers.length, Seq())
    if (randomNumbers.size % 2 == 0) randomNumbers else randomNumbers :+ randomNumbers.head
  }.map(index => existingUsers(index))

  /** generate random numbers from 0 to n, without duplicates */
  def generateRandomNumbers(n: Int, numbers: Seq[Int]): Seq[Int] = {
    def add(x: Int) = if (numbers.contains(x)) numbers else numbers :+ x
    if (numbers.length == n) numbers
    else generateRandomNumbers(n, add(Random.nextInt(n)))
  }



  /** send notification to user */
  def sendMail(to: String, content: String) {
    import com.typesafe.plugin._
    val mail = use[MailerPlugin].email
    mail.setSubject("Поздравь друга!")
    mail.setRecipient(to)
    mail.setFrom(Play.current.configuration.getString("smtp.from").get)
    mail.send(content)
  }

  /** general report with order (for test) */
  def sendTestReport(content: Html) {
    import com.typesafe.plugin._
    val mail = use[MailerPlugin].email
    mail.setSubject("Тест работы Кампус-подарок")
    mail.setRecipient(testEmail)
    mail.setFrom(Play.current.configuration.getString("smtp.from").get)
    mail.sendHtml(content.toString)
  }


}
