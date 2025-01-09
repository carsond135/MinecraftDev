package io.mcdev.obfex.mappings

import com.intellij.openapi.vfs.VirtualFile

interface MappingsFormatParser {

    val expectedFileExtensions: Array<String>

    fun isSupportedFile(file: VirtualFile): Boolean = false

    fun parse(file: VirtualFile): Mapp
}