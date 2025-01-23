package io.mcdev.obfex.mappings

import io.mcdev.obfex.mappings.store.MappingLookupTable
import io.mcdev.obfex.ref.ArrayTypeDef
import io.mcdev.obfex.ref.ClassName
import io.mcdev.obfex.ref.ClassTypeDef
import io.mcdev.obfex.ref.FieldRef
import io.mcdev.obfex.ref.MethodDescriptor
import io.mcdev.obfex.ref.MethodRef
import io.mcdev.obfex.ref.PackName
import io.mcdev.obfex.ref.PrimitiveTypeDef
import io.mcdev.obfex.ref.ReturnTypeDef
import io.mcdev.obfex.ref.VoidTypeDef
import io.mcdev.obfex.ref.asClass
import io.mcdev.obfex.ref.asPackage

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
        when (typeDef) {
            is VoidTypeDef, is PrimitiveTypeDef -> return typeDef
        }

        val fromNamespace = findNamespace(fromNs)
        val toNamespace = findNamespace(toNs)
        if (fromNamespace === toNamespace) {
            return typeDef
        }

        @Suppress("UNCHECKED_CAST")
        return when (typeDef) {
            is ArrayTypeDef -> ArrayTypeDef(
                mapType(fromNamespace, toNamespace, typeDef.componentType), typeDef.dimension
            ) as T

            is ClassTypeDef -> {
                val name = classStore.forNamespace(fromNamespace)
                    .query(typeDef.className.name)
                    .firstOrNull()
                    ?.fullyQualifiedName(toNamespace)
                    ?.asClass()
                    ?: typeDef.className
                ClassTypeDef(name) as T
            }

            else -> throw IllegalStateException("Unexpected typedef: " + typeDef.javaClass.name)
        }
    }

    fun mapType(fromNs: MappingNamespace, toNs: MappingNamespace, desc: MethodDescriptor): MethodDescriptor {
        return MethodDescriptor(desc.params.map { mapType(fromNs, toNs, it) }, mapType(fromNs, toNs, desc.returnType))
    }

    fun <T : ReturnTypeDef> mapTypeTo(ns: MappingNamespace, typeDef: T): T {
        return mapType(typeNamespace, ns, typeDef)
    }

    fun mapTypeTo(ns: MappingNamespace, desc: MethodDescriptor): MethodDescriptor {
        return mapType(typeNamespace, ns, desc)
    }

    fun <T : ReturnTypeDef> mapTypeFrom(ns: MappingNamespace, typeDef: T): T {
        return mapType(ns, typeNamespace, typeDef)
    }

    fun mapTypeFrom(ns: MappingNamespace, desc: MethodDescriptor): MethodDescriptor {
        return mapType(ns, typeNamespace, desc)
    }

    fun packs(): List<PackageMappingElement> {
        return packageStore.list()
    }

    fun packs(name: PackName): List<PackageMappingElement> {
        return packageStore.byName.query(name.name).list()
    }
    fun packs(name: String): List<PackageMappingElement> = packs(name.asPackage())

    fun pack(name: PackName): PackageMappingElement? {
        return packageStore.byName.query(name.name).firstOrNull()
    }
    fun pack(name: String): PackageMappingElement? = pack(name.asPackage())

    fun packs(ns: MappingNamespace, name: PackName): List<PackageMappingElement> {
        return packageStore.forNamespace(ns).query(name.name).list()
    }
    fun packs(ns: MappingNamespace, name: String): List<PackageMappingElement> =
        packs(ns, name.asPackage())

    fun pack(ns: MappingNamespace, name: PackName): PackageMappingElement? {
        return packageStore.forNamespace(ns).query(name.name).firstOrNull()
    }
    fun pack(ns: MappingNamespace, name: String): PackageMappingElement? = pack(ns, name.asPackage())

    fun addPackageMapping(names: Array<String?>, location: MappingLocation): PackageMappingElement {
        val element = PackageMappingElement(names, this, location)
        packageStore.add(element)
        return element
    }

    fun classes(): List<ClassMappingElement> {
        return classStore.list()
    }

    fun classes(name: ClassName): List<ClassMappingElement> {
        return classStore.byName.query(name.name).list()
    }
    fun classes(name: String): List<ClassMappingElement> = classes(name.asClass())

    fun clazz(name: ClassName): ClassMappingElement? {
        return classStore.byName.query(name.name).firstOrNull()
    }
    fun clazz(name: String): ClassMappingElement? = clazz(name.asClass())

    fun classes(ns: MappingNamespace, name: ClassName): List<ClassMappingElement> {
        return classStore.forNamespace(ns).query(name.name).list()
    }
    fun classes(ns: MappingNamespace, name: String): List<ClassMappingElement> = classes(ns, name.asClass())

    fun clazz(ns: MappingNamespace, name: ClassName): ClassMappingElement? {
        return classStore.forNamespace(ns).query(name.name).firstOrNull()
    }
    fun clazz(ns: MappingNamespace, name: String): ClassMappingElement? = clazz(ns, name.asClass())

    fun addClassMapping(
        names: Array<String?>,
        parent: ClassMappingElement?,
        location: MappingLocation
    ): ClassMappingElement {
        val element = ClassMappingElement(names, parent, this, location)
        classStore.add(element)
        return element
    }

    fun fieldMapping(field: FieldRef, ns: MappingNamespace): FieldMappingElement? {
        val clazz = clazz(ns, field.containingClass) ?: return null
        return clazz.field(ns, field.field)
    }

    fun fieldMapping(field: FieldRef): FieldMappingElement? {
        return namespaces.firstNotNullOfOrNull { ns -> fieldMapping(field, ns) }
    }

    fun methodMapping(method: MethodRef, ns: MappingNamespace): MethodMappingElement? {
        val clazz = clazz(ns, method.containingClass) ?: return null
        return clazz.method(ns, method.method)
    }

    fun methodMapping(method: MethodRef): MethodMappingElement? {
        return namespaces.firstNotNullOfOrNull { ns -> methodMapping(method, ns) }
    }
}