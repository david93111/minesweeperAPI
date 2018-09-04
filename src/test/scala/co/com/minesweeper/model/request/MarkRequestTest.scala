package co.com.minesweeper.model.request

import co.com.minesweeper.BaseTest
import co.com.minesweeper.model.MarkType

import scala.util.Try

class MarkRequestTest extends BaseTest{

  "MarkRequest Test should" -{

    "Fail if parameters don't meet the requirements" in {

      Try(MarkRequest(-1,0,MarkType.FlagMark))
        .failed
        .map(ex => ex shouldBe classOf[IllegalArgumentException])

    }

    "Work if parameters meet the requirements" in {

      val mark = MarkRequest(0,0,MarkType.FlagMark)

      mark.mark shouldEqual MarkType.FlagMark

    }

  }

}
