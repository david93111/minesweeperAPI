package co.com.minesweeper.model.request

import co.com.minesweeper.BaseTest
import co.com.minesweeper.config.AppConf

import scala.util.Try

class NewGameRequestTest extends BaseTest{

  "New Game Request Test should " - {

    "Allow creation of class even if some parameters are missing" in {

      val request = NewGameRequest("user1")

      request.mines shouldEqual None

    }

    "Allow Creation of Game Settings from New Game Request with valid fields" in {

      val request  = NewGameRequest("user2", Some(3), Some(4))
      val settings = NewGameRequest.GameSettingsFromGameRequest(request)

      settings.mines shouldEqual AppConf.defaultMines
      settings.user shouldEqual "user2"
      settings.rows shouldEqual 3
      settings.columns shouldEqual 4

    }

    "Fail to create Game Settings from New Game Request with invalid values" in {

      val request  = NewGameRequest("user2", Some(-1), Some(0), Some(0))
      Try(NewGameRequest.GameSettingsFromGameRequest(request))
        .failed.map(_ shouldBe classOf[IllegalArgumentException])

    }

  }

}
