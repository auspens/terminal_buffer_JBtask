import buffer.TerminalBuffer
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class TerminaBufferClearAndFillFunctionsTests {

    @Test
    fun `clearScreen resets content but preserves scrollback`() {
        val buffer = TerminalBuffer(5, 2, 5)
        buffer.writeTextOverriding("OLD\nNEW\nNEWER")

        buffer.clearScreen()

        assertEquals("     ", buffer.getScreenLine(0))
        assertEquals("     ", buffer.getScreenLine(1))
        assertEquals("OLD  ", buffer.getBufferLine(0))
        assertEquals(Pair(0, 0), buffer.getCursorPosition())
    }

    @Test
    fun `clearAll resets content incuding scrollback`() {
        val buffer = TerminalBuffer(5, 2, 5)
        buffer.writeTextOverriding("OLD\nNEW\nNEWER")

        buffer.clearAll()

        assertEquals("     ", buffer.getScreenLine(0))
        assertEquals("     ", buffer.getScreenLine(1))
        assertEquals("     ", buffer.getBufferLine(0))
        assertEquals(Pair(0, 0), buffer.getCursorPosition())
    }

    @Test
    fun `fillCurrentLine fills line with given char`() {
        val buffer = TerminalBuffer(5, 3, 5)
        buffer.writeTextOverriding("OLD\nNEW\nNEWER\n")

        buffer.fillCurrentLine('u')
        buffer.setCursorPosition(0, 0)
        buffer.fillCurrentLine()
        buffer.setCursorPosition(1, 3)
        buffer.fillCurrentLine('a')

        assertEquals("     ", buffer.getScreenLine(0))
        assertEquals("aaaaa", buffer.getScreenLine(1))
        assertEquals("uuuuu", buffer.getScreenLine(2))
        assertEquals("OLD  ", buffer.getBufferLine(0))
    }
}