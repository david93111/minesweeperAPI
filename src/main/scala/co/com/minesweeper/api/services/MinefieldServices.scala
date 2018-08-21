package co.com.minesweeper.api.services

import co.com.minesweeper.model.{Field, FieldType}

import scala.util.Random

object MinefieldServices {

  def createMinefield(columns: Int = 9, rows: Int = 9, maxMines: Int = 41): Array[Array[Field]] ={

    val minefield: Array[Array[Field]] = Array.tabulate[Field](rows,columns){ (_,_) =>
      Field(FieldType.Empty)
    }

    // Fill minefield with random bombs
    for (_ <- 1 to maxMines){
      putBomb(columns, rows, minefield)
    }

    minefield

  }

  // Array is mutable by default, so is modified by reference
  // In case of mine already putted, recursive call is used
  private def putBomb(columns: Int, rows: Int, matrix: Array[Array[Field]] ): Unit ={
    val random = new Random()
    val col = random.nextInt(columns)
    val row = random.nextInt(rows)

    val field = matrix(row)(col)

    field.fieldType match {
      case FieldType.Mine =>
        putBomb(columns, rows, matrix)
      case _ =>
        matrix(row)(col) = Field(FieldType.Mine)
    }

  }

}
