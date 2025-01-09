package io.mcdev.obfex.formats

import java.util.Collections

class MappingsFormatTypeManager {

    private val types: HashMap<String, MappingsFormatType> = hashMapOf()

    fun registerType(type: MappingsFormatType) {
        if (types.containsKey(type.id)) {
            throw IllegalArgumentException("Cannot register mappings format type: ${type.id}; Already registered")
        }
        types[type.id] = type
    }

    val registeredTypes: Collection<MappingsFormatType>
        get() = Collections.unmodifiableCollection(types.values)

    companion object {
        private val instance = MappingsFormatTypeManager()
        fun get() = instance
    }
}