# Kotlin Terminal Buffer

A terminal buffer implementation designed for fixed-width grid environments. 

## 🏗️ Architectural Design Decisions

### 1. Storage Strategy: ArrayDeque
The core storage uses an `ArrayDeque<MutableList<Cell>>`. 
* **Performance:** Terminal buffers are essentially specialized FIFO queues. `ArrayDeque` provides **$O(1)$** complexity for adding new lines to the tail and removing oldest lines from the head (scrolling). This avoids the **$O(n)$** memory-shifting penalty of a standard `ArrayList`.
* **Random Access:** Unlike a `LinkedList`, the `ArrayDeque` allows for $O(1)$ random access, which is critical for rendering specific lines or accessing scrollback history.



### 2. Null-Safety & The "Display Character" Logic
Internally, the buffer distinguishes between a "space character" and an "empty cell."
* **Internal Representation:** Cells are initialized with `null` characters. This preserves the "Empty" state, allowing for future logic like smart background rendering or "Trim Trailing Whitespace" during copy operations.
* **External Transformation:** When accessing content via `getScreenLine()` or `getBufferLine()`, the buffer automatically transforms `null` values into `' '` (space). 
* **Why?** This ensures that any external consumer (like a UI renderer or a text file exporter) receives an aligned, fixed-width string without having to handle nullability themselves.



### 3. Dual Coordinate Systems
To decouple the user's view from the stored data, the buffer implements two indexing strategies:
* **Relative Screen Space:** Indices from `0` to `height - 1`. This is the "Viewport" the user sees.
* **Absolute Buffer Space:** Indices from `0` to `totalLineCount - 1`. This includes the entire history (Scrollback + Screen).
A dedicated mapping function `getGlobalRowIndex()` handles the translation.



### 4. Fixed-Width Integrity
Every row is maintained as a `MutableList` of exactly `width` elements. Operations like `writeTextInserting` handle overflow by truncating characters that shift past the boundary, maintaining a strict grid.

## 🚀 Key Features
* **Overriding & Inserting:** Support for standard character overwriting and modern text insertion with character shifting.
* **Scrollback History:** Configurable `maxScrollBack` to limit memory usage while preserving history.
* **Rich Attributes:** Every cell tracks its own Foreground Color, Background Color, and Font Style.
* **Cursor Navigation:** Comprehensive cursor movement API with boundary clamping.

## 📁 Project Structure
```text
src/
├── main/
│   └── kotlin/
│       ├── buffer/
│       │   └── TerminalBuffer.kt   <-- Core Logic
│       └── models/
│           ├── Cell.kt            <-- Data Model (Handles Null -> Space)
│           ├── Color.kt           <-- Color Enums
│           └── Style.kt           <-- Formatting Data
└── test/
    └── kotlin/
        ├── TerminalBufferAccessTest.kt
        ├── TerminalBufferInsertTest.kt
        └── TerminalBufferWriteTest.kt
```

## 🛠️ Getting Started

### Prerequisites
* **JDK 17+**
* **Gradle 8.x** (Wrapper included)

### Setup & Testing
1. Clone the repository.
2. Run the unit tests to verify the buffer logic:
   ```bash
   ./gradlew test
   ```
   
## 📝 Usage Example

```Kotlin
// 1. Initialize an 80x24 terminal with 100 lines of scrollback history
val buffer = TerminalBuffer(width = 80, height = 24, maxScrollBack = 100)

// 2. Set global styling for subsequent writes
buffer.setAttributes(
    fg = Color.GREEN, 
    bg = Color.BLACK, 
    style = Style(bold = true)
)

// 3. Write text (Standard Overriding)
// This moves the cursor to (1, 0) automatically due to the newline
buffer.writeTextOverriding("Welcome to the Terminal\n")

// 4. Move cursor and Insert text (Shifting)
// This will shift "the Terminal" to the right to make room for "Kotlin "
buffer.setCursorPosition(row = 0, column = 11)
buffer.writeTextInserting("Kotlin ") 

// 5. Access Content
// Internal nulls are transformed into spaces (' ') for the caller
val line = buffer.getScreenLine(0) 
println(line) // Output: "Welcome to Kotlin Terminal... [trailing spaces]"

// 6. Navigate the history
val totalLines = buffer.getTotalLineCount()
val firstLineEver = buffer.getBufferLine(0)
```
