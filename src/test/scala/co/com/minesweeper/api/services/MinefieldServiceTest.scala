package co.com.minesweeper.api.services

import co.com.minesweeper.BaseTest
import co.com.minesweeper.model.Field

class MinefieldServiceTest extends BaseTest{

  "Minefield Service Test Should" - {

    "Create a new minefield with given parameters" in {

      val minefield = MinefieldService.createMinefield(3, 3, 1)

      minefield.mines shouldEqual 1
      minefield.board.length shouldEqual 3
      minefield.board.head.length shouldEqual 3

    }

    "Create a new minefield and reveal a field and all empty near fields" in {

      val minefield = MinefieldService.createMinefield(2, 2, 0)

      val validateCellOperation: (Int, Int) => Boolean = MinefieldService.cellInValidRange(minefield.columns, minefield.rows)

      val selectedSpot: Field = minefield.board(0)(0)

      MinefieldService.revealSpot(minefield.board)(0,0,selectedSpot)
      MinefieldService.revealSpotsUntilHintOrMine(minefield.board, MinefieldService.nearSpots(0,0), validateCellOperation)

      minefield.board.foreach(_.foreach(field => field.revealed shouldEqual true))

    }

    "Create a new minefield and reveal only selected field if surrounded by bombs" in {

      val minefield = MinefieldService.createMinefield(2, 2, 4)

      val validateCellOperation: (Int, Int) => Boolean = MinefieldService.cellInValidRange(minefield.columns, minefield.rows)

      val selectedSpot: Field = minefield.board(0)(0)

      MinefieldService.revealSpot(minefield.board)(0,0,selectedSpot)
      MinefieldService.revealSpotsUntilHintOrMine(minefield.board, MinefieldService.nearSpots(0,0), validateCellOperation)

      minefield.board.map(_.toList).toList.flatten.count(_.revealed) shouldEqual 1

    }

  }

}
