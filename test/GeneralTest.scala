import org.specs2.mutable._
import controllers._


class GeneralTest extends Specification {

  "Function generateRandomNumbers(10) return List" should {
    "must contains 10 elemnts" in {
      Application.generateRandomNumbers(10, Seq()) must have size(10)
    }
    "not contain duplicates" in {
      val numbers = Application.generateRandomNumbers(10, Seq())
      numbers must have size(numbers.distinct.length)
    }
  }
}