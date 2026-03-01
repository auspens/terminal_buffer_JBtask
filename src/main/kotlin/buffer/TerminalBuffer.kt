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

    private val allLines = ArrayDeque<MutableList<Cell>>()

    init {
        require(width > 0)
        require(height > 0)
        require(maxScrollBack >= 0)
        repeat(height){ allLines.add(createEmptyLine()) }
    }
    private fun createEmptyLine() = MutableList(width){ Cell() }

    fun insertEmptyLineAtBottom(){
        allLines.addLast(createEmptyLine())
        if (allLines.size > height + maxScrollBack)
            allLines.removeFirst()
    }

    fun getGlobalRowIndex(row:Int):Int{
        val screenStart = (allLines.size - height).coerceAtLeast(0)
        return screenStart + row
    }

    fun writeTextOverriding(text:String){
        var currentRow = allLines[getGlobalRowIndex(cursorRow)]
        for (c in text){
            if (cursorColumn == width || c == '\n'){
                handleWrap()
                currentRow = allLines[getGlobalRowIndex(cursorRow)]
                if (c == '\n') continue
            }
            currentRow[cursorColumn] = Cell(c, currentForeground, currentBackground, currentStyle)
            cursorColumn++
        }
    }

    private fun handleWrap(){
        if(cursorRow >= height - 1){
            insertEmptyLineAtBottom()
            cursorRow = height - 1
        }
        else cursorRow++
        cursorColumn = 0
    }

    fun setAttributes(fg:Color, bg:Color, style:Style){
        this.currentForeground = fg
        this.currentBackground = bg
        this.currentStyle = style
    }

    //content access functions
    fun getTotalLineCount(): Int = allLines.size
    fun getScrollBackLineCount(): Int {
        return (allLines.size - height).coerceAtLeast(0)
    }
    fun getScreenLineCount(): Int = height
    fun getScreenChar(row: Int, col: Int): Char{
        require(row in 0..<height){"Row index out of range"}
        require (col in 0..<width){"Column index out of range"}
        return allLines[getGlobalRowIndex(row)][col].displayChar
    }
    fun getBufferChar(row: Int, col: Int): Char{
        require(row in allLines.indices){"Row index out of range"}
        require (col in 0..<width){"Column index out of range"}
        return allLines[row][col].displayChar
    }
    fun getScreenLine(row: Int): String{
        require(row in 0..<height){"Row index out of range"}
        return allLines[getGlobalRowIndex(row)].joinToString(""){ it.displayChar.toString() }
    }
    fun getBufferLine(row: Int): String{
        require(row in allLines.indices){"Row index out of range"}
        return allLines[row].joinToString(""){it.displayChar.toString()}
    }
    fun getScreenContent(): String {
        val start = getGlobalRowIndex(0)
        val end = allLines.size - 1
        return (start..end).joinToString("\n") { getBufferLine(it) }
    }
    fun getFullContent(): String {
        return allLines.joinToString("\n") { line ->
            line.joinToString("") { it.displayChar.toString() }
        }
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