package co.com.minesweeper.api.services

import co.com.minesweeper.BaseTest

class MinefieldServiceTest extends BaseTest{

  "Minefield Service Test Should" - {

    "Create a new minefield with given parameters" in {

      val minefield = MinefieldService.createMinefield(3, 3, 1)

      minefield.mines shouldEqual 1
      minefield.board.length shouldEqual 3
      minefield.board.head.length shouldEqual 3

    }

  }

}
