package io.mcdev.obfex

import com.google.common.base.CharMatcher
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.util.ArrayUtil
import kotlin.contracts.contract

enum class Tristate {
    TRUE,
    FALSE,
    UNKNOWN,
}

inline fun hash(func: Hasher.() -> Unit): Int {
    val hasher = Hasher()
    hasher.func()
    return hasher.code
}

class Hasher {
    var code: Int = 1
        private set

    private fun updateHash(value: Int) {
        code = 31 * code + value
    }

    operator fun Any?.unaryPlus() {
        updateHash(this?.hashCode() ?: 0)
    }
    operator fun Array<*>?.unaryPlus() {
        updateHash(contentHashCode())
    }
    operator fun ByteArray?.unaryPlus() {
        updateHash(contentHashCode())
    }
    operator fun ShortArray?.unaryPlus() {
        updateHash(contentHashCode())
    }
    operator fun IntArray?.unaryPlus() {
        updateHash(contentHashCode())
    }
    operator fun LongArray?.unaryPlus() {
        updateHash(contentHashCode())
    }
    operator fun FloatArray?.unaryPlus() {
        updateHash(contentHashCode())
    }
    operator fun DoubleArray?.unaryPlus() {
        updateHash(contentHashCode())
    }
    operator fun BooleanArray?.unaryPlus() {
        updateHash(contentHashCode())
    }
    operator fun CharArray?.unaryPlus() {
        updateHash(contentHashCode())
    }

    operator fun Byte?.invoke() {
        updateHash(this?.hashCode() ?: 0)
    }
    operator fun Byte.invoke() {
        updateHash(this.hashCode())
    }
    operator fun Short?.invoke() {
        updateHash(this?.hashCode() ?: 0)
    }
    operator fun Short.invoke() {
        updateHash(this.hashCode())
    }
    operator fun Int?.invoke() {
        updateHash(this?.hashCode() ?: 0)
    }
    operator fun Int.invoke() {
        updateHash(this.hashCode())
    }
    operator fun Long?.invoke() {
        updateHash(this?.hashCode() ?: 0)
    }
    operator fun Long.invoke() {
        updateHash(this.hashCode())
    }
    operator fun Float?.invoke() {
        updateHash(this?.hashCode() ?: 0)
    }
    operator fun Float.invoke() {
        updateHash(this.hashCode())
    }
    operator fun Double?.invoke() {
        updateHash(this?.hashCode() ?: 0)
    }
    operator fun Double.invoke() {
        updateHash(this.hashCode())
    }
    operator fun Boolean?.invoke() {
        updateHash(this?.hashCode() ?: 0)
    }
    operator fun Boolean.invoke() {
        updateHash(this.hashCode())
    }
    operator fun Char?.invoke() {
        updateHash(this?.hashCode() ?: 0)
    }
    operator fun Char.invoke() {
        updateHash(this.hashCode())
    }
}

private fun <T> emptyArrayUtil(): Array<T> {
    @Suppress("UNCHECKED_CAST")
    return ArrayUtil.EMPTY_OBJECT_ARRAY as Array<T>
}

data class MappingPart(val col: Int, val value: String)

private val whitespace = CharMatcher.whitespace()
private val nonWhitespace = whitespace.negate()

fun String.splitMappingLine(preserveBlank: Boolean = false): Array<MappingPart> {
    var index = 0
    if (this.isNotEmpty() && whitespace.matches(this[0])) {
        index = nonWhitespace.indexIn(this)
        if (index == -1) {
            return emptyArrayUtil()
        }
    }

    if (this[index] == '#') {
        return emptyArrayUtil()
    }

    var arrayIndex = 0
    var array = arrayOfNulls<MappingPart>(6)
    while (true) {
        if (preserveBlank) {
            val nextPart = if (index >= this.length) {
                break
            } else if (whitespace.matches(this[index])) {
                ""
            } else {
                val endIndex = whitespace.indexIn(this, index)
                if (endIndex == -1) {
                    this.substring(index, this.length)
                } else {
                    this.substring(index, endIndex)
                }
            }

            if (arrayIndex >= array.size) {
                array = array.copyOf(array.size * 2)
            }

            array[arrayIndex++] = MappingPart(index, nextPart)
            index += nextPart.length + 1
        } else {
            val startIndex = nonWhitespace.indexIn(this, index)
            if (this[startIndex] == '#') {
                break
            }

            if (arrayIndex >= array.size) {
                array = array.copyOf(array.size * 2)
            }

            val endIndex = whitespace.indexIn(this, startIndex + 1)
            if (endIndex == -1) {
                array[arrayIndex++] = MappingPart(startIndex, this.substring(startIndex))
                break
            } else {
                array[arrayIndex++] = MappingPart(startIndex, this.substring(startIndex, endIndex))
                index = endIndex
            }
        }
    }

    if (arrayIndex == 0) {
        return emptyArrayUtil()
    }

    @Suppress("UNCHECKED_CAST")
    return array.copyOf(arrayIndex) as Array<MappingPart>
}

fun String.isJavaInternalIdentifier(): Boolean {
    if (this.isEmpty()) {
        return false
    }

    var isStart = true
    var index = 0
    while (true) {
        if (index >= this.length) {
            break
        }

        val c = this.codePointAt(index++)
        if (Character.isSupplementaryCodePoint(c)) {
            index++
        }

        when {
            isStart -> {
                isStart = false
                if (!Character.isJavaIdentifierStart(c)) {
                    return false
                }
            }
            c == '/'.code || c == '.'.code -> {
                isStart = true
                continue
            }
            else -> {
                if (!Character.isJavaIdentifierPart(c)) {
                    return false
                }
            }
        }
    }

    return true
}

typealias LineBlock = (lineNum: Int, parts: Array<MappingPart>, line: String) -> Boolean
inline fun VirtualFile.forLines(skipLines: Int = 0, preserveBlank: Boolean = false, block: LineBlock) {
    contract {
        callsInPlace(block)
    }

    this.inputStream.bufferedReader(this.charset).use { reader ->
        var lineNum = 0
        while (true) {
            lineNum++
            val line = reader.readLine() ?: break
            if (lineNum <= skipLines) {
                continue
            }

            val parts = line.splitMappingLine(preserveBlank = preserveBlank)
            if (parts.isEmpty()) {
                continue
            }

            if (!block(lineNum, parts, line)) {
                break
            }
        }
    }
}

typealias IndentedLineBlock = (indent: Int, lineNum: Int, parts: Array<MappingPart>, line: String) -> Boolean
inline fun VirtualFile.forLinesIndent(skipLines: Int = 0, preserveBlank: Boolean = false, block: IndentedLineBlock) {
    contract {
        callsInPlace(block)
    }

    this.forLines(skipLines = skipLines, preserveBlank = preserveBlank) { lineNum, parts, line ->
        val index = line.firstNonWhitespaceIndex()
        if (index < 0) {
            return@forLines true
        }

        return@forLines block(index, lineNum, parts, line)
    }
}

fun String.firstNonWhitespaceIndex(): Int = nonWhitespace.indexIn(this)

fun String.splitOnLast(delimiter: Char): Pair<String, String?> {
    val lastIndex = lastIndexOf(delimiter)
    if (lastIndex < 0) {
        return this to null
    }

    return substring(0, lastIndex) to substring(lastIndex + 1)
}
fun MappingPart.splitOnLast(delimiter: Char): Pair<String, String?> = value.splitOnLast(delimiter)