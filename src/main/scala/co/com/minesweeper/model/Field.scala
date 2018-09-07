package co.com.minesweeper.model

/** A minefield box representation.
  *
  *  @author david93111
  *  @constructor create a new Field with given parameters.
  *  @param fieldType Logical field type, Mine, Empty or Hint
  *  @param row row in which the field is located
  *  @param col row in which the field is located
  *  @param content Optional content to indicate near mines in case of hint
  *  @param revealed the person's age in years
  *  @param mark the person's age in years
  */
case class Field(fieldType: FieldType,
                  row: Int,
                  col: Int,
                  content: Option[Int] = None,
                  revealed: Boolean = false,
                  mark: MarkType = MarkType.None)

