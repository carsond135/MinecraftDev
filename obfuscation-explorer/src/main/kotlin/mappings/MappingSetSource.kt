package io.mcdev.obfex.mappings

import com.intellij.openapi.vfs.VirtualFile
import io.mcdev.obfex.formats.MappingsFormatType

data class MappingParseIssue(val message: String, val coords: FileCoords?)

interface MappingIssuesRegistry {
    fun error(message: String, coords: FileCoords? = null)
    fun warning(message: String, coords: FileCoords? = null)
}

sealed interface MappingSetSource {
    val errors: List<MappingParseIssue>
    val warnings: List<MappingParseIssue>
}

class MappingsFile(
    val file: VirtualFile,
    val type: MappingsFormatType,
    override val errors: List<MappingParseIssue> = emptyList(),
    override val warnings: List<MappingParseIssue> = emptyList(),
) : MappingSetSource

class MappingsFileBuilder(
    val file: VirtualFile,
    val type: MappingsFormatType,
) : MappingIssuesRegistry {

    val errors: MutableList<MappingParseIssue> = mutableListOf()
    val warnings: MutableList<MappingParseIssue> = mutableListOf()

    override fun error(message: String, coords: FileCoords?) {
        errors.add(MappingParseIssue(message, coords))
    }

    override fun warning(message: String, coords: FileCoords?) {
        warnings.add(MappingParseIssue(message, coords))
    }

    fun build() = MappingsFile(file, type, errors.toList(), warnings.toList())
}