package io.mcdev.obfex.lookup

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap

class HashLookupIndex<T, K>(
    private val table: HashLookupTable<T>,
    private val transformer: LookupIndexTransformer<T, K>,
) : LookupIndex<T, K> {

    private var map: Object2ObjectOpenHashMap<K?, HashLookupTable<T>>? = null

    override fun query(key: K?): MultiLookupTable<T> {
        return map?.get(key) ?: MultiLookupTable.empty()
    }

    @Suppress("unused")
    private fun rebuild() {
        @Suppress("UNCHECKED_CAST")
        val store = tableList.get(table.unwrap(HashLookupTable::class)) as ObjectLinkedOpenHashSet<T>

        map = Object2ObjectOpenHashMap(store.size)
    }
}