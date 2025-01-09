package io.mcdev.obfex.lookup

import kotlin.reflect.KClass

interface LookupIndex<T, K> {

    fun query(key: K?): MultiLookupTable<T>

    fun <L : LookupIndex<*, *>> unwrap(type: KClass<L>): L?
}