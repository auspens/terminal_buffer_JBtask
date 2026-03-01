package buffer

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class TerminalBufferInsertTest {

    @Test
    fun `insert shifts characters right`() {
        val buffer = TerminalBuffer(5, 2, 10)

        buffer.writeTextOverriding("ABCDE")
        buffer.setCursorPosition(0, 2)

        buffer.writeTextInserting("X")

        assertEquals("ABXCD", buffer.getScreenLine(0))
        assertEquals(Pair(0, 3), buffer.getCursorPosition())
    }

    @Test
    fun `insert at beginning shifts whole line`() {
        val buffer = TerminalBuffer(5, 2, 10)

        buffer.writeTextOverriding("ABCDE")
        buffer.setCursorPosition(0, 0)

        buffer.writeTextInserting("Z")

        assertEquals("ZABCD", buffer.getScreenLine(0))
        assertEquals(Pair(0, 1), buffer.getCursorPosition())
    }

    @Test
    fun `insert at last column drops last character`() {
        val buffer = TerminalBuffer(5, 2, 10)

        buffer.writeTextOverriding("ABCDE")
        buffer.setCursorPosition(0, 4)

        buffer.writeTextInserting("X")

        assertEquals("ABCDX", buffer.getScreenLine(0))
        assertEquals(Pair(0, 5), buffer.getCursorPosition())
    }

    @Test
    fun `insert newline moves to next line without changing the current line to the right`() {
        val buffer = TerminalBuffer(5, 2, 10)

        buffer.writeTextOverriding("ABCDE")
        buffer.setCursorPosition(0, 2)

        buffer.writeTextInserting("\nX")

        assertEquals("ABCDE", buffer.getScreenLine(0))
        assertEquals("X    ", buffer.getScreenLine(1))
        assertEquals(Pair(1, 1), buffer.getCursorPosition())
    }

    @Test
    fun `insert multiple characters shifts progressively`() {
        val buffer = TerminalBuffer(5, 2, 10)

        buffer.writeTextOverriding("ABCDE")
        buffer.setCursorPosition(0, 1)

        buffer.writeTextInserting("XY")

        assertEquals("AXYBC", buffer.getScreenLine(0))
        assertEquals(Pair(0, 3), buffer.getCursorPosition())
    }

    @Test
    fun `insert at bottom wraps and creates scrollback`() {
        val buffer = TerminalBuffer(5, 2, 10)

        buffer.writeTextOverriding("Line1\nLine2")

        buffer.writeTextInserting("Z")

        assertEquals(3, buffer.getTotalLineCount())
        assertEquals(1, buffer.getScrollBackLineCount())

        assertEquals("Line2", buffer.getBufferLine(1))
        assertEquals("Z    ", buffer.getScreenLine(1))
    }

    @Test
    fun `insert does not affect other lines`() {
        val buffer = TerminalBuffer(5, 3, 10)

        buffer.writeTextOverriding("AAAAA\nBBBBB\nCCCCC")
        buffer.setCursorPosition(1, 2)

        buffer.writeTextInserting("X")

        assertEquals("AAAAA", buffer.getScreenLine(0))
        assertEquals("BBXBB", buffer.getScreenLine(1))
        assertEquals("CCCCC", buffer.getScreenLine(2))
    }
}