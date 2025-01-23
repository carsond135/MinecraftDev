@file:Suppress("MemberVisibilityCanBePrivate")

package io.mcdev.obfex.mappings

import io.mcdev.obfex.ref.ClassName
import io.mcdev.obfex.ref.LinePriority
import io.mcdev.obfex.ref.LocalMemberRef
import io.mcdev.obfex.ref.MemberName

interface MappingSetBuilderCore {
    fun pack(line: LinePriority? = null, col: Int = 1): PackageMappingBuilder
    fun clazz(line: LinePriority? = null, col: Int = 1): ClassMappingBuilder

    fun clazz(ref: NS<ClassName>, line: LinePriority? = null, col: Int = 1): ClassMappingBuilder
}

class MappingSetBuilder(
    val issues: MappingIssuesRegistry,
    vararg namespaces: String,
) : MappingSetBuilderCore, MappingIssuesRegistry by issues {

    val namespaces: Array<String> = if (namespaces.isEmpty()) {
        arrayOf(UNNAMED_FROM, UNNAMED_TO)
    } else {
        @Suppress("UNCHECKED_CAST")
        namespaces as Array<String>
    }

    val packageMappings = ArrayList<PackageMappingBuilder>()
    val classMappings = ArrayList<ClassMappingBuilder>()

    private var lastClassMapping: ClassMappingBuilder? = null

    override fun pack(line: LinePriority?, col: Int): PackageMappingBuilder {
        return PackageMappingBuilder(namespaces, this, FileCoords(line, col)).also { packageMappings += it }
    }

    override fun clazz(line: LinePriority?, col: Int): ClassMappingBuilder {
        return ClassMappingBuilder(namespaces, this, FileCoords(line, col)).also { classMappings += it }
    }

    override fun clazz(ref: NS<ClassName>, line: LinePriority?, col: Int): ClassMappingBuilder {
        if (lastClassMapping?.names?.get(ref.ns) == ref.v.name) {
            return lastClassMapping!!
        }
        val res =
            classMappings.firstOrNull { it.names[ref.ns] == ref.v.name }
                ?: ClassMappingBuilder(namespaces, this, FileCoords(line, col)).also {
                    classMappings += it
                    it.with(ref)
                }
        lastClassMapping = res
        return res
    }

    fun build(): MappingSet {
        val set = MappingSet(namespaces.asIterable())

        packageMappings.forEach { it.build(set, issues) }
        classMappings.forEach { it.build(set, issues) }

        return set
    }
}

abstract class NamingBuilder<T : MemberName>(val namespaces: Array<String>) {

    val names: Array<String?> = arrayOfNulls(namespaces.size)

    operator fun set(ns: String, name: T) {
        set(namespaces.indexOf(ns), name)
    }

    operator fun set(index: Int, name: T) {
        with(NS(name, index))
    }

    fun with(ref: NS<T>) {
        addName(ref.forName())
    }

    fun unlessExists(ref: NS<T>) {
        if (names[ref.ns] != null) {
            return
        }
        addName(ref.forName())
    }

    protected abstract fun addName(ref: NS<String>)
}

data class NS<V : Any>(val v: V, val ns: Int) {
    fun <T : Any> withValue(newV: T): NS<T> = NS(newV, ns)
    inline fun <T : Any> withValue(block: V.() -> T): NS<T> = NS(v.block(), ns)
}
fun NS<out MemberName>.forName(): NS<String> = NS(v.name, ns)

fun <T : MemberName> T.ns(ns: Int): NS<T> = NS(this, ns)
fun <T : LocalMemberRef<*>> T.ns(ns: Int): NS<T> = NS(this, ns)