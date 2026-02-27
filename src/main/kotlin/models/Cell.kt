package models

data class Cell(
    val char: Char? = null,
    val foreground: Color = Color.DEFAULT,
    val background: Color = Color.DEFAULT,
    val style: Style = Style()
)
{
    val displayChar: Char get() = char ?: ' '
}