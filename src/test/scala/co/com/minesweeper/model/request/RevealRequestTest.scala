package co.com.minesweeper.model.request

import co.com.minesweeper.BaseTest

import scala.util.Try

class RevealRequestTest extends BaseTest{

  "Reveal Request Test Should" - {

    "Create a new reveal request if parameters are valid" in {
      val revealRequest: RevealRequest = RevealRequest(0,0)
      revealRequest.col shouldEqual 0
    }

    "Fail for create a new reveal request with invalid parameters" in {

      Try(RevealRequest(-1,0))
        .failed
        .map(_ shouldBe classOf[IllegalArgumentException])

    }


  }

}
