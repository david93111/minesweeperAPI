package co.com.minesweeper.model

case class Field(fieldType: FieldType, content: Option[Int] = None, revealed: Boolean = false, mark: Option[MarkType] = None)

