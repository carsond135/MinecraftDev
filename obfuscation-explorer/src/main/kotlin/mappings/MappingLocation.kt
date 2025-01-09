package io.mcdev.obfex.mappings

import io.mcdev.obfex.MappingPart
import io.mcdev.obfex.ref.LinePriority

sealed interface MappingLocation {
    val priority: Int

    fun withPriority(priority: Int): MappingLocation {
        return when (this) {
            is FileCoords -> copy(priority = priority)
            is UnknownLocation -> this
        }
    }
}

data class FileCoords(val line: Int, val col: Int = 1, override val priority: Int = 0) : MappingLocation {
    constructor(line: LinePriority?, col: Int = 1) : this(line?.coord ?: -1, col, line?.priority ?: 0)
    constructor(line: LinePriority?, part: MappingPart) : this(line, part.col)
    constructor(line: Int, part: MappingPart) : this(line, part.col)

    override fun withPriority(priority: Int): FileCoords {
        return super.withPriority(priority) as FileCoords
    }
}

data object UnknownLocation : MappingLocation {
    override val priority: Int
        get() = Int.MIN_VALUE

    override fun withPriority(priority: Int): MappingLocation {
        return this
    }
}