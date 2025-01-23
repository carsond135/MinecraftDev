package io.mcdev.obfex.mappings.store

import io.mcdev.obfex.mappings.MappingElement

data class NamespaceLookup<T : MappingElement>(val index: Int, val lookup: MappingLookupIndex<T, String>)