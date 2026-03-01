package buffer

import models.Cell
import models.Color
import models.Style

/**
 * A terminal buffer implementing a fixed-width grid with scrollback history.
 *
 * ### Key Design Decisions:
 * - **Storage:** Uses [ArrayDeque] to ensure O(1) performance for scroll operations (adding to
 * tail and removing from head), avoiding the O(n) overhead of standard list shifts.
 * - **Coordinate Systems:** Decouples "Screen Space" (0 to height-1) from "Buffer Space"
 * (absolute indices) to allow the viewport to remain stable as history grows.
 * - **Memory Efficiency:** Rows are maintained as fixed-width [MutableList]s to prevent
 * fragmentation and ensure predictable memory usage.
 * - **State Representation:** Uses a [Cell] model where null characters represent
 * uninitialized space, distinguishing them from explicit space (' ') characters.
 */

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

    /**
     * Writes text starting at the current cursor position, replacing existing characters.
     * * If the text exceeds the current line [width], it triggers a wrap to the next row.
     * If the cursor is at the bottom of the [height], a new line is appended and the
     * oldest line is moved to scrollback history.
     * * @param text The string to be written into the buffer.
     */
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

    /**
     * Inserts text at the current cursor position, shifting existing characters to the right.
     * * Characters pushed beyond the [width] of the line are truncated (dropped).
     * This operation maintains the fixed-width grid integrity while allowing
     * mid-line editing.
     * * @param text The string to be inserted.
     */
    fun writeTextInserting(text:String) {
        var currentRow = allLines[getGlobalRowIndex(cursorRow)]
        for (char in text) {
            if (char == '\n' || cursorColumn >= width) {
                handleWrap()
                currentRow = allLines[getGlobalRowIndex(cursorRow)]
                if (char == '\n') continue
            }
            currentRow.add(
                cursorColumn,
                Cell(char, currentForeground, currentBackground, currentStyle)
            )
            currentRow.removeAt(width)
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

    /**
     * Fills every cell of the current cursor row with the specified character.
     * Useful for visual separators or clearing specific lines with background colors.
     * * @param char The character to fill the line with. Defaults to null (empty).
     * Doesn't change cursor position.
     */
    fun fillCurrentLine(char: Char? = null) {
        val currentRow = allLines[getGlobalRowIndex(cursorRow)]
        for (i in 0 until width) {
            currentRow[i] = Cell(char, currentForeground, currentBackground, currentStyle)
        }
    }

    /**
     * Resets all cells within the visible screen area to empty.
     * Moves the cursor to the top-left (0,0). Existing scrollback history is preserved.
     */
    fun clearScreen() {
        val screenStart = allLines.size - height
        for (i in screenStart until allLines.size) {
            allLines[i] = createEmptyLine()
        }
        setCursorPosition(0, 0)
    }

    /**
     * Resets all cells within the visible screen area and scrollback history to empty.
     * Moves the cursor to the top-left (0,0).
     */
    fun clearAll() {
        allLines.clear()
        repeat(height) { allLines.add(createEmptyLine()) }
        setCursorPosition(0, 0)
    }

    fun insertEmptyLineAtBottom(){
        allLines.addLast(createEmptyLine())
        if (allLines.size > height + maxScrollBack)
            allLines.removeFirst()
    }

    fun setAttributes(fg:Color, bg:Color, style:Style){
        this.currentForeground = fg
        this.currentBackground = bg
        this.currentStyle = style
    }

    /**
     * Retrieves the visual styling of a specific cell in the buffer.
     * * @param row The absolute index in the buffer (0 to totalLineCount - 1).
     * @param col The column index (0 to width - 1).
     * @return A Triple containing (Foreground Color, Background Color, Style).
     */
    fun getAttributesAt(row: Int, col: Int): Triple<Color, Color, Style> {
        require(row in allLines.indices) { "Buffer row out of range" }
        require(col in 0 until width) { "Column index out of range" }

        val cell = allLines[row][col]
        return Triple(cell.foreground, cell.background, cell.style)
    }

    /**
     * Returns the total number of lines currently stored in the buffer,
     * including both the visible screen and the scrollback history.
     */
    fun getTotalLineCount(): Int = allLines.size

    /**
     * Returns the number of lines that have scrolled off the top of the visible screen.
     */
    fun getScrollBackLineCount(): Int {
        return (allLines.size - height).coerceAtLeast(0)
    }

    /**
     * Returns the fixed height of the visible terminal screen.
     */
    fun getScreenLineCount(): Int = height

    /**
     * Retrieves the character at a specific coordinate on the visible screen.
     * @param row The row index relative to the screen (0 to height-1).
     * @param col The column index (0 to width-1).
     */
    fun getScreenChar(row: Int, col: Int): Char{
        require(row in 0..<height){"Row index out of range"}
        require (col in 0..<width){"Column index out of range"}
        return allLines[getGlobalRowIndex(row)][col].displayChar
    }

    /**
     * Retrieves the character at an absolute position in the entire buffer.
     * @param row The absolute row index in the history (0 to totalLineCount-1).
     * @param col The column index (0 to width-1).
     */
    fun getBufferChar(row: Int, col: Int): Char{
        require(row in allLines.indices){"Row index out of range"}
        require (col in 0..<width){"Column index out of range"}
        return allLines[row][col].displayChar
    }

    /**
     * Returns a string representation of a specific line on the visible screen.
     * Missing characters are represented as spaces to maintain fixed width.
     */
    fun getScreenLine(row: Int): String{
        require(row in 0..<height){"Row index out of range"}
        return allLines[getGlobalRowIndex(row)].joinToString(""){ it.displayChar.toString() }
    }

    /**
     * Returns a string representation of a specific line from the absolute buffer history.
     */
    fun getBufferLine(row: Int): String{
        require(row in allLines.indices){"Row index out of range"}
        return allLines[row].joinToString(""){it.displayChar.toString()}
    }

    /**
     * Concatenates all currently visible lines on the screen into a single
     * newline-delimited string.
     */
    fun getScreenContent(): String {
        val start = getGlobalRowIndex(0)
        val end = allLines.size - 1
        return (start..end).joinToString("\n") { getBufferLine(it) }
    }

    /**
     * Concatenates the entire buffer history, including scrollback and
     * visible screen, into a single newline-delimited string.
     */
    fun getFullContent(): String {
        return allLines.joinToString("\n") { line ->
            line.joinToString("") { it.displayChar.toString() }
        }
    }

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

    /**
     * Internal helper to handle line-wrapping and scrolling.
     * Increments [cursorRow] or triggers [insertEmptyLineAtBottom] if at the viewport edge.
     */
    private fun createEmptyLine() = MutableList(width){ Cell() }

    /**
     * Maps a relative screen coordinate to an absolute index in the [allLines] storage.
     * * @param row The row relative to the visible screen (0 = top of screen).
     * @return The global index in the underlying ArrayDeque.
     */
    fun getGlobalRowIndex(row:Int):Int{
        val screenStart = (allLines.size - height).coerceAtLeast(0)
        return screenStart + row
    }
}