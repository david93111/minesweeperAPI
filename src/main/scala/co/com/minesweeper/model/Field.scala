package co.com.minesweeper.model

case class Field(fieldType: FieldType, row: Int, col: Int, content: Option[Int] = None, revealed: Boolean = false, mark: MarkType = MarkType.None)

