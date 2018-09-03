package co.com.minesweeper.api.services

import co.com.minesweeper.model.{Field, FieldType, Minefield}
import org.slf4j.{Logger, LoggerFactory}

import scala.util.Random

object MinefieldService {

  val log: Logger = LoggerFactory.getLogger(this.getClass)

  val nearSpots: (Int,Int) => List[(Int, Int)]= (startRow, startColumn) => List(
    (startRow - 1, startColumn - 1),
    (startRow, startColumn - 1),
    (startRow + 1, startColumn - 1),
    (startRow - 1, startColumn),
    (startRow + 1, startColumn),
    (startRow - 1, startColumn + 1),
    (startRow, startColumn + 1),
    (startRow + 1, startColumn + 1)
  )

  def createMinefield(columns: Int, rows: Int, maxMines: Int): Minefield ={

    val minefield: Array[Array[Field]] = Array.tabulate[Field](rows,columns){ (_,_) =>
      Field(FieldType.Empty)
    }

    // Fill minefield with random bombs
    for (_ <- 1 to maxMines){
      putBomb(columns, rows, minefield)
    }

    val validateRange: (Int, Int) => Boolean = cellInValidRange(columns,rows)

    val reviewNear: (Int, Int) => Int = reviewNearFields(_, _, minefield, validateRange)

    val arrayWithHints =  Array.tabulate(rows, columns) { (row, col) =>
        val spot: Field = minefield(row)(col)
        spot.fieldType match {
          case FieldType.Empty =>
            val numOfNearMines = reviewNear(row, col)
            if(numOfNearMines > 0) Field(FieldType.Hint,Some(numOfNearMines)) else spot
          case _ => spot
        }
    }

    Minefield(arrayWithHints, rows, columns, maxMines)
  }

  def revealSpotsUntilHintOrMine(board: Array[Array[Field]], cellsToReveal: List[(Int,Int)], validCell: (Int,Int)=> Boolean): Int = {
    cellsToReveal.map{ case (row, col) =>
      if(validCell(row,col)){
        val spot = board(row)(col)
        spot.fieldType match {
          case FieldType.Empty if !spot.revealed =>
            revealSpot(board)(row, col, spot)
            1 + revealSpotsUntilHintOrMine(board, nearSpots(row, col), validCell)
          case FieldType.Hint if !spot.revealed =>
            revealSpot(board)(row, col, spot)
            1
          case _ => 0
        }
      }else 0
    }.sum
  }

  def revealSpot(minefield: Array[Array[Field]])(row: Int, col: Int, field: Field): Unit ={
    minefield(row)(col) = field.copy(revealed = true)
  }

  private def reviewNearFields(startRow:Int, startColumn: Int, board: Array[Array[Field]], validateFunction:(Int,Int) => Boolean) ={

    val getSpot: (Int, Int) => Option[Field] = (row,col) => if(validateFunction(row,col)) Some(board(row)(col)) else None

    val list = nearSpots(startRow, startColumn).map{ spot =>
      getSpot(spot._1, spot._2).fold(0)(_.fieldType match {
        case FieldType.Mine => 1
        case _ => 0
      })
    }

    list.sum

  }

  def cellInValidRange(maxCol: Int, maxRows: Int)(row: Int, col:Int): Boolean = {
    (row >= 0 && row < maxRows) && (col >=0 && col < maxCol)
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

  def printArrayUtility(array: Array[Array[Field]]): Unit = {
    array.foreach{innerArray =>
      innerArray.foreach { field =>
        val rev = if (field.revealed) "r" else "n"
        field.fieldType match {
          case FieldType.Mine => print("*")
          case FieldType.Hint => print(field.content.get)
          case FieldType.Empty => print("E")
        }
        print(rev)
        print("|")
      }
      println("")
    }
  }

}
