package io.mcdev.obfex.mappings

import io.mcdev.obfex.mappings.store.MappingLookupTable
import io.mcdev.obfex.ref.ReturnTypeDef

@Suppress("MemberVisibilityCanBePrivate")
class MappingSet(namespaceNames: Iterable<String> = emptyList()) {

    val typeNamespace: MappingNamespace

    val namespaces: Array<MappingNamespace>

    private val packageStore = MappingLookupTable<PackageMappingElement>(this)
    private val classStore = MappingLookupTable<ClassMappingElement>(this)

    init {
        val names = namespaceNames.toList()
        if (names.isEmpty()) {
            typeNamespace = MappingNamespace(MappingNamespace.UNNAMED_FROM, 0, this)
            namespaces = arrayOf(typeNamespace, MappingNamespace(MappingNamespace.UNNAMED_TO, 1, this))
        } else if (names.size >= 2) {
            namespaces = Array(names.size) { index -> MappingNamespace(names[index], index, this) }
            typeNamespace = namespaces[0]
        } else {
            throw IllegalArgumentException("namespaceNames must contain at least 2 namespaces: $names")
        }
    }

    fun namespaceOf(name: String): MappingNamespace = namespaceOfOrNull(name)
        ?: throw IllegalArgumentException("Could not find namespace: $name")

    fun namespaceOfOrNull(name: String): MappingNamespace? = namespaces.find { it.name == name }

    private fun findNamespace(ns: MappingNamespace): MappingNamespace = if (ns.associatedSet === this) {
        ns
    } else {
        namespaces.find { it.name == ns.name }
            ?: throw IllegalArgumentException(
                "Provided namespace is not associated with the current MappingSet: ${ns.name}"
            )
    }

    fun namespaceIndex(name: String): Int {
        return namespaceIndex(namespaceOf(name))
    }

    fun namespaceIndex(ns: MappingNamespace): Int = findNamespace(ns).index

    fun <T : ReturnTypeDef> mapType(fromNs: MappingNamespace, toNs: MappingNamespace, typeDef: T): T {
        
    }
}