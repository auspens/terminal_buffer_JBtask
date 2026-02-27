package buffer

import models.Cell
import models.Color
import models.Style

class TerminalBuffer (
    val width:Int,
    val height:Int,
    val maxScrollBack: Int)
{
    private var cursorX:Int = 0
    private var cursorY: Int = 0

    private var currentBackground= Color.DEFAULT
    private var currentForeground = Color.DEFAULT
    private var currentStyle = Style()

    private val lines = mutableListOf<MutableList<Cell>>()

    init {
        repeat(height){ lines.add(createEmptyLine()) }
    }
    private fun createEmptyLine() = MutableList(width){ Cell() }
}