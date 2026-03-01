import buffer.TerminalBuffer
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class TerminalBufferWriteOverrideTest {
    @Test
    fun `initial screen is empty`() {
        val buffer = TerminalBuffer(width = 5, height = 3, maxScrollBack = 10)

        for (row in 0 until 3) {
            assertEquals("     ", buffer.getScreenLine(row))
        }
        assertEquals(3, buffer.getTotalLineCount())
        assertEquals(0, buffer.getScrollBackLineCount())
    }

    @Test
    fun `write text without wrap`() {
        val buffer = TerminalBuffer(5, 3, 10)

        buffer.writeTextOverriding("Hi")

        assertEquals("Hi   ", buffer.getScreenLine(0))
        assertEquals(Pair(0, 2), buffer.getCursorPosition())
    }

    @Test
    fun `write text wraps to next line`() {
        val buffer = TerminalBuffer(5, 3, 10)

        buffer.writeTextOverriding("ABCDEZ")

        assertEquals("ABCDE", buffer.getScreenLine(0))
        assertEquals("Z    ", buffer.getScreenLine(1))
        assertEquals(Pair(1, 1), buffer.getCursorPosition())

        buffer.setCursorPosition(0,0)
        buffer.writeTextOverriding("FGH\n")
        assertEquals("FGHDE", buffer.getScreenLine(0))
        assertEquals("Z    ", buffer.getScreenLine(1))
        assertEquals(Pair(1, 0), buffer.getCursorPosition())
    }

    @Test
    fun `writing past bottom creates scrollback`() {
        val buffer = TerminalBuffer(width = 5, height = 2, maxScrollBack = 10)

        buffer.writeTextOverriding("Line1\nLine2\nLine3")

        assertEquals(3, buffer.getTotalLineCount())
        assertEquals(1, buffer.getScrollBackLineCount())

        assertEquals("Line2", buffer.getScreenLine(0))
        assertEquals("Line3", buffer.getScreenLine(1))

        assertEquals("Line1", buffer.getBufferLine(0))
    }

    @Test
    fun `scrollback is limited by maxScrollBack`() {
        val buffer = TerminalBuffer(width = 5, height = 2, maxScrollBack = 1)

        buffer.writeTextOverriding("L1\nL2\nL3\nL4")

        assertEquals(3, buffer.getTotalLineCount())
        assertEquals(1, buffer.getScrollBackLineCount())

        assertEquals("L2   ", buffer.getBufferLine(0))
    }
}