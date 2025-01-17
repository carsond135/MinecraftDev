package io.mcdev.obfex.mappings.store

import io.mcdev.obfex.lookup.LookupIndex
import io.mcdev.obfex.mappings.MappingElement
import io.mcdev.obfex.mappings.MappingSet
import kotlin.reflect.KClass

class MappingLookupIndex<T : MappingElement, K>(
    private val set: MappingSet,
    private val index: LookupIndex<T, K>
) : LookupIndex<T, K> by index {

    override fun query(key: K?): MappingLookupTable<T> {
        return MappingLookupTable(set, index.query(key))
    }

    override fun <L : LookupIndex<*, *>> unwrap(type: KClass<L>): L? {
        if (type.isInstance(this)) {
            @Suppress("UNCHECKED_CAST")
            return this as L
        }
        return index.unwrap(type)
    }
}