package io.mcdev.obfex.mappings

data class MappingNamespace(
    val name: String,
    val index: Int,
    val associatedSet: MappingSet?,
) {
    companion object {
        const val UNNAMED_FROM: String = "from"
        const val UNNAMED_TO: String = "to"

        fun unnamedFrom(set: MappingSet) = set.namespaceOf(UNNAMED_FROM)
        fun unnamedTo(set: MappingSet) = set.namespaceOf(UNNAMED_TO)
    }
}