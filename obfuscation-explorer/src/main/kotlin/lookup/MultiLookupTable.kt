package io.mcdev.obfex.lookup

import java.util.Collections
import kotlin.reflect.KClass

typealias SingleLookupIndexTransformer<T, K> = (T) -> K?
typealias LookupIndexTransformer<T, K> = (T) -> List<K>

interface MultiLookupTable<T> {

    fun seq(): Sequence<T>

    fun list(): List<T>

    fun firstOrNull(): T? {
        return seq().firstOrNull()
    }

    fun singleOrNull(): T? {
        return seq().singleOrNull()
    }

    fun isEmpty(): Boolean = size == 0

    val size: Int

    fun <K> indexMulti(transformer: LookupIndexTransformer<T, K>): LookupIndex<T, K>

    fun <K> index(transformer: SingleLookupIndexTransformer<T, K>): LookupIndex<T, K> = indexMulti {
        listOfNotNull(transformer(it))
    }

    fun add(value: T)

    fun remove(value: T)

    fun <L : MultiLookupTable<*>> unwrap(type: KClass<L>): L?

    companion object {
        private val emptyTable = HashLookupTable<Any>()

        @Suppress("UNCHECKED_CAST")
        fun <T> empty(): MultiLookupTable<T> = emptyTable as MultiLookupTable<T>
    }
}

inline fun <T> MultiLookupTable<T>.query(predicate: (T) -> Boolean): List<T> =
    Collections.unmodifiableList(list().filter(predicate))