import buffer.TerminalBuffer
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertEquals

class TerminalBufferCursorTest {
    @Test
    fun `cursor starts at 0, 0`(){
        val buffer = TerminalBuffer(10,10,10)
        val cursor = buffer.getCursorPosition()
        assertEquals(0, cursor.first)
        assertEquals(0, cursor.second)
    }

    @Test
    fun `set cursor position inside the screen`(){
        val buffer = TerminalBuffer(10,10,10)
        buffer.setCursorPosition(5,3)
        val cursor = buffer.getCursorPosition()
        assertEquals(5, cursor.first)
        assertEquals(3, cursor.second)
    }

    @Test
    fun `set cursor position outside row bounds`(){
        val buffer = TerminalBuffer(10,5,10)
        buffer.setCursorPosition(15,3)
        var cursor = buffer.getCursorPosition()
        assertEquals(4, cursor.first)
        buffer.setCursorPosition(-15,-3)
        cursor = buffer.getCursorPosition()
        assertEquals(0, cursor.first)
        assertEquals(0, cursor.second)
    }

    @Test
    fun `set cursor position outside column bounds`(){
        val buffer = TerminalBuffer(10,5,10)
        buffer.setCursorPosition(5,13)
        val cursor = buffer.getCursorPosition()
        assertEquals(9, cursor.second)
    }

    @Test
    fun `move cursor up decreases row`(){
        val buffer = TerminalBuffer(10,10,10)
        buffer.setCursorPosition(5,3)
        var cursor = buffer.getCursorPosition()
        assertEquals(5, cursor.first)
        buffer.moveCursorUp(2)
        cursor = buffer.getCursorPosition()
        assertEquals(3, cursor.first)
    }

    @Test
    fun `move cursor down increases row`(){
        val buffer = TerminalBuffer(10,10,10)
        buffer.setCursorPosition(5,3)
        var cursor = buffer.getCursorPosition()
        assertEquals(5, cursor.first)
        buffer.moveCursorDown(2)
        cursor = buffer.getCursorPosition()
        assertEquals(7, cursor.first)
    }

    @Test
    fun `move cursor left decreases column`(){
        val buffer = TerminalBuffer(10,10,10)
        buffer.setCursorPosition(5,3)
        var cursor = buffer.getCursorPosition()
        assertEquals(3, cursor.second)
        buffer.moveCursorLeft(2)
        cursor = buffer.getCursorPosition()
        assertEquals(1, cursor.second)
    }

    @Test
    fun `move cursor right increases column`(){
        val buffer = TerminalBuffer(10,10,10)
        buffer.setCursorPosition(5,3)
        var cursor = buffer.getCursorPosition()
        assertEquals(3, cursor.second)
        buffer.moveCursorRight(2)
        cursor = buffer.getCursorPosition()
        assertEquals(5, cursor.second)
    }

    @Test
    fun `move cursor up outside bounds clamps`(){
        val buffer = TerminalBuffer(10,5,10)
        var cursor = buffer.getCursorPosition()
        assertEquals(0, cursor.first)
        buffer.moveCursorUp(2)
        cursor = buffer.getCursorPosition()
        assertEquals(0, cursor.first)
    }

    @Test
    fun `move cursor down outside bounds clamps`(){
        val buffer = TerminalBuffer(10,5,10)
        var cursor = buffer.getCursorPosition()
        assertEquals(0, cursor.first)
        buffer.moveCursorDown(12)
        cursor = buffer.getCursorPosition()
        assertEquals(4, cursor.first)
    }

    @Test
    fun `move cursor left outside bounds clamps`(){
        val buffer = TerminalBuffer(5,10,10)
        var cursor = buffer.getCursorPosition()
        assertEquals(0, cursor.second)
        buffer.moveCursorLeft(2)
        cursor = buffer.getCursorPosition()
        assertEquals(0, cursor.second)
    }

    @Test
    fun `move cursor right outside bounds clamps`(){
        val buffer = TerminalBuffer(5,10,10)
        var cursor = buffer.getCursorPosition()
        assertEquals(0, cursor.second)
        buffer.moveCursorRight(12)
        cursor = buffer.getCursorPosition()
        assertEquals(4, cursor.second)
    }

    @Test
    fun `move cursor negative throws exception`(){
        val buffer = TerminalBuffer(5,10,10)
        assertThrows<IllegalArgumentException> { buffer.moveCursorDown(-2) }
        assertThrows<IllegalArgumentException> { buffer.moveCursorUp(-2) }
        assertThrows<IllegalArgumentException> { buffer.moveCursorLeft(-2) }
        assertThrows<IllegalArgumentException> { buffer.moveCursorRight(-2) }
    }
}