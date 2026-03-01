import buffer.TerminalBuffer
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertEquals

class TerminalBufferAccessFunctionsTest {

    @Test
    fun `getScreenChar returns correct character`() {
        val buffer = TerminalBuffer(5, 2, 10)

        buffer.writeTextOverriding("D\nABC\nC")

        assertEquals('A', buffer.getScreenChar(0, 0))
        assertEquals('B', buffer.getScreenChar(0, 1))
        assertEquals('C', buffer.getScreenChar(0, 2))
        assertEquals(' ', buffer.getScreenChar(0, 4))
    }

    @Test
    fun `getBufferChar returns correct character`() {
        val buffer = TerminalBuffer(5, 2, 10)

        buffer.writeTextOverriding("ABC\nD\nE")
        val row = 10 - buffer.getScrollBackLineCount()

        assertEquals('A', buffer.getBufferChar(0, 0))
        assertEquals('B', buffer.getBufferChar(0, 1))
        assertEquals('C', buffer.getBufferChar(0, 2))
        assertEquals(' ', buffer.getBufferChar(0, 4))
    }

    @Test
    fun `getFullContent includes scrollback`() {
        val buffer = TerminalBuffer(5, 2, 10)

        buffer.writeTextOverriding("A\nB\nC")

        val content = buffer.getFullContent()

        assertTrue(content.contains("A"))
        assertTrue(content.contains("B"))
        assertTrue(content.contains("C"))
    }

    @Test
    fun `getScreenContent gets only screen content`() {
        val buffer = TerminalBuffer(5, 2, 10)

        buffer.writeTextOverriding("A\nB\nC")

        val content = buffer.getScreenContent()

        assertTrue(!content.contains("A"))
        assertTrue(content.contains("B"))
        assertTrue(content.contains("C"))
    }
    @Test
    fun `getScreenLine gets correct screen line`() {
        val buffer = TerminalBuffer(5, 2, 10)

        buffer.writeTextOverriding("A\nB\nC")

        val content = buffer.getScreenLine(buffer.getScreenLineCount()-1)

        assertTrue(!content.contains("A"))
        assertTrue(!content.contains("B"))
        assertTrue(content.contains("C"))
    }
    @Test
    fun `getBufferLine gets correct buffer line`() {
        val buffer = TerminalBuffer(5, 2, 10)

        buffer.writeTextOverriding("A\nB\nC")

        val content = buffer.getBufferLine(buffer.getTotalLineCount()-1)

        assertTrue(!content.contains("A"))
        assertTrue(!content.contains("B"))
        assertTrue(content.contains("C"))

        val content0 = buffer.getBufferLine(0)
        assertTrue(content0.contains("A"))
        assertTrue(!content0.contains("B"))
        assertTrue(!content0.contains("C"))
    }

    @Test
    fun `access functions throw for invalid indices`(){
        val buffer = TerminalBuffer(5, 2, 10)

        buffer.writeTextOverriding("A\nB\nC")

        assertThrows<IllegalArgumentException> { buffer.getBufferChar(-1, 0) }
        assertThrows<IllegalArgumentException> { buffer.getBufferLine(3) }
        assertThrows<IllegalArgumentException> { buffer.getScreenChar(0, 6) }
        assertThrows<IllegalArgumentException> { buffer.getScreenLine(2) }
    }
}