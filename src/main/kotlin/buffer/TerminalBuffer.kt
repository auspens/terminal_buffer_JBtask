package buffer

import models.Cell
import models.Color
import models.Style

class TerminalBuffer (
    val width:Int,
    val height:Int,
    val maxScrollBack: Int)
{
    private var cursorRow:Int = 0
    private var cursorColumn: Int = 0

    private var currentBackground= Color.DEFAULT
    private var currentForeground = Color.DEFAULT
    private var currentStyle = Style()

    private val lines = mutableListOf<MutableList<Cell>>()

    init {
        repeat(height){ lines.add(createEmptyLine()) }
    }
    private fun createEmptyLine() = MutableList(width){ Cell() }

    fun setAttributes(fg:Color, bg:Color, style:Style){
        this.currentForeground = fg
        this.currentBackground = bg
        this.currentStyle = style
    }
 //cursor functions
    private fun clampRow(row:Int)= row.coerceIn(0, height - 1)
    private fun clampColumn(column:Int)= column.coerceIn(0, width - 1)

    fun setCursorPosition(row:Int, column:Int){
        cursorRow = clampRow(row)
        cursorColumn = clampColumn(column)
    }
    fun getCursorPosition(): Pair<Int, Int> = Pair(cursorRow, cursorColumn)

    fun moveCursorUp(rows:Int = 1) {
        require(rows >= 0) { "Move should be non-negative" }
        cursorRow = clampRow(cursorRow - rows)
    }
    fun moveCursorDown(rows:Int = 1){
        require(rows >= 0) { "Move should be non-negative" }
        cursorRow = clampRow(cursorRow + rows)
    }
    fun moveCursorLeft(columns:Int = 1){
        require(columns >= 0) { "Move should be non-negative" }
        cursorColumn = clampColumn(cursorColumn - columns)
    }
    fun moveCursorRight(columns:Int = 1){
        require(columns >= 0) { "Move should be non-negative" }
        cursorColumn = clampColumn(cursorColumn + columns)
    }
}