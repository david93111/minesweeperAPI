package co.com.minesweeper.api.services

import co.com.minesweeper.model.{Field, FieldType}
import org.slf4j.{Logger, LoggerFactory}

import scala.util.Random

object MinefieldServices {

  val log: Logger = LoggerFactory.getLogger(this.getClass)
  def createMinefield(columns: Int = 9, rows: Int = 9, maxMines: Int = 41): Array[Array[Field]] ={

    val minefield: Array[Array[Field]] = Array.tabulate[Field](rows,columns){ (_,_) =>
      Field(FieldType.Empty)
    }

    // Fill minefield with random bombs
    for (_ <- 1 to maxMines){
      putBomb(columns, rows, minefield)
    }

    val validateRange: (Int, Int) => Boolean = cellInValidRange(columns,rows)

   val array =  Array.tabulate(rows, columns) { (row, col) =>
      val spot: Field = minefield(row)(col)
      spot.fieldType match {
        case FieldType.Empty =>
          val numOfNearMines = reviewNearFields(row,col,minefield,validateRange)
          if(numOfNearMines > 0) Field(FieldType.Hint,Some(numOfNearMines)) else spot
        case _ => spot
      }
    }


    array.foreach{innerArray =>
      innerArray.foreach { field =>
        field.fieldType match {
          case FieldType.Mine => print("*")
          case FieldType.Hint => print(field.content.get)
          case FieldType.Empty => print(" ")
        }
        print("|")
      }
      println("")
    }

    array
  }

  private def reviewNearFields(startRow:Int, startColumn: Int,board: Array[Array[Field]], validateFunction:(Int,Int) => Boolean) ={

    val getSpot: (Int, Int) => Option[Field] = (row,col) => if(validateFunction(row,col)) Some(board(row)(col)) else None

    val nearSpots = List(
      (startRow - 1, startColumn - 1),
      (startRow, startColumn - 1),
      (startRow + 1, startColumn - 1),
      (startRow - 1, startColumn),
      (startRow + 1, startColumn),
      (startRow - 1, startColumn + 1),
      (startRow, startColumn + 1),
      (startRow + 1, startColumn + 1)
    )

    val list = nearSpots.map{ spot =>
      getSpot(spot._1, spot._2).fold(0)(_.fieldType match {
        case FieldType.Mine => 1
        case _ => 0
      })
    }

    list.sum

  }

  private def cellInValidRange(maxCol: Int, maxRows: Int)(row: Int, col:Int): Boolean = {
    row >= 0 && row < maxRows && col >=0 && col < maxCol
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
