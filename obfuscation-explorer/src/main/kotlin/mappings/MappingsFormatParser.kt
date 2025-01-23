package io.mcdev.obfex.mappings

import com.intellij.openapi.extensions.ExtensionPointName
import com.intellij.openapi.vfs.VirtualFile
import io.mcdev.obfex.mappings.MappingsFormatParser.Companion.EP_NAME
import io.mcdev.obfex.ref.LocalMemberRef
import io.mcdev.obfex.ref.MemberName

interface MappingsFormatParser {

    val expectedFileExtensions: Array<String>

    fun isSupportedFile(file: VirtualFile): Boolean = false

    fun parse(file: VirtualFile): MappingsDefinition?

    companion object {
        @JvmStatic
        val EP_NAME = ExtensionPointName.create<MappingsFormatParser>("io.mcdev.obfex.mappingsFormatParser")
    }
}

interface UnnamedMappingsFormatParser : MappingsFormatParser {

    val unnamedFrom: Int
        get() = 0

    val unnamedTo: Int
        get() = 1

    val <T : MemberName> T.from: NS<T>
        get() = ns(unnamedFrom)
    val <T : MemberName> T.to: NS<T>
        get() = ns(unnamedTo)

    val <T : LocalMemberRef<*>> T.from: NS<T>
        get() = ns(unnamedFrom)
    val <T : LocalMemberRef<*>> T.to: NS<T>
        get() = ns(unnamedTo)
}