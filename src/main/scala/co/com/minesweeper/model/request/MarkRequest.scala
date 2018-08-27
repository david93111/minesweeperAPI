package co.com.minesweeper.model.request

import co.com.minesweeper.model.MarkType

case class MarkRequest(row: Int, col: Int, mark: MarkType)
