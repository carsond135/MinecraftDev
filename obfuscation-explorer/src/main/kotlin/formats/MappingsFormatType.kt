package io.mcdev.obfex.formats

import com.intellij.util.xmlb.Converter as IConverter
import javax.swing.Icon
import org.jetbrains.annotations.NonNls

abstract class MappingsFormatType(@param:NonNls val id: String) {

    abstract val icon: Icon
    abstract val name: String

    class Converter : IConverter<MappingsFormatType>() {
        override fun toString(value: MappingsFormatType): String? {
            return value.id
        }

        override fun fromString(value: String): MappingsFormatType? {
            return MappingsFormatTypeManager.get().registeredTypes.firstOrNull { it.id == value }
        }
    }
}