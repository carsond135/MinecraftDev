package io.mcdev.obfex.ref

import io.mcdev.obfex.MappingPart
import it.unimi.dsi.fastutil.ints.Int2ObjectLinkedOpenHashMap

interface MemberName {
    val name: String
}

@JvmInline
value class PackName(override val name: String) : MemberName
@JvmInline
value class ClassName(override val name: String) : MemberName
@JvmInline
value class FieldName(override val name: String) : MemberName
@JvmInline
value class MethodName(override val name: String) : MemberName
@JvmInline
value class ParamName(override val name: String) : MemberName
@JvmInline
value class LocalVarName(override val name: String) : MemberName

fun String.asPackage(): PackName {
    val replaced = this.replace('.', '/')
    return if (replaced == "//") {
        PackName("/")
    } else {
        PackName(replaced)
    }
}
fun String.asClass(): ClassName = ClassName(this.replace('.', '/'))
fun String.asField(): FieldName = FieldName(this)
fun String.asMethod(): MethodName = MethodName(this)
fun String.asParam(): ParamName = ParamName(this)
fun String.asLocal(): LocalVarName = LocalVarName(this)

fun MappingPart.asPackage(): PackName = value.asPackage()
fun MappingPart.asClass(): ClassName = value.asClass()
fun MappingPart.asField(): FieldName = value.asField()
fun MappingPart.asMethod(): MethodName = value.asMethod()
fun MappingPart.asParam(): ParamName = value.asParam()
fun MappingPart.asLocal(): LocalVarName = value.asLocal()

interface LocalMemberRef<T : MemberName> {
    val name: T
}

data class LocalFieldRef(override val name: FieldName, val type: TypeDef? = null) : LocalMemberRef<FieldName> {
    fun withType(type: TypeDef?): LocalFieldRef = copy(type = type)
    fun withoutType(): LocalFieldRef = withType(null)
}

data class FieldRef(val containingClass: ClassName, val field: LocalFieldRef)

fun FieldName.asRef(type: TypeDef? = null): LocalFieldRef = LocalFieldRef(this, type)
fun String.asFieldRef(type: TypeDef? = null): LocalFieldRef = this.asField().asRef(type)
fun String.asFieldRef(type: String?): LocalFieldRef = this.asField().asRef(type?.asTypeDef())

fun ClassName.field(name: FieldName, type: TypeDef? = null): FieldRef = FieldRef(this, name.asRef(type))
fun ClassName.field(ref: LocalFieldRef): FieldRef = FieldRef(this, ref)

data class LocalMethodRef(override val name: MethodName, val desc: MethodDescriptor?) : LocalMemberRef<MethodName> {
    fun withDesc(desc: MethodDescriptor): LocalMethodRef = copy(desc = desc)
}

data class MethodRef(val containingClass: ClassName, val method: LocalMethodRef)

fun MethodName.asRef(desc: MethodDescriptor? = null): LocalMethodRef = LocalMethodRef(this, desc)
fun String.asMethodRef(desc: MethodDescriptor? = null): LocalMethodRef = this.asMethod().asRef(desc)
fun String.asMethodRef(desc: String?): LocalMethodRef = this.asMethod().asRef(desc?.asMethodDesc())

fun ClassName.method(name: MethodName, desc: MethodDescriptor? = null): MethodRef = MethodRef(this, name.asRef(desc))
fun ClassName.method(ref: LocalMethodRef): MethodRef = MethodRef(this, ref)

@JvmInline
value class ParamIndex(val index: Int)

data class LocalVarIndex(val index: Local, val localStart: Local = Local.UNKNOWN, val localEnd: Local = Local.UNKNOWN) {
    val isKnown: Boolean
        get() = index.isKnown

    companion object {
        @JvmStatic
        val UNKNOWN = LocalVarIndex(Local.UNKNOWN)
    }
}

@JvmInline
value class LvtIndex(val index: Local) {
    val isKnown: Boolean
        get() = this.index.isKnown

    companion object {
        @JvmStatic
        val UNKNOWN = LvtIndex(Local.UNKNOWN)
    }
}

@JvmInline
value class Local(val index: Int) {
    val isKnown: Boolean
        get() = this.index != -1

    companion object {
        @JvmStatic
        val UNKNOWN = Local(-1)
    }
}

fun Int.asParamIndex(): ParamIndex = ParamIndex(this)
fun Int.asLocal(): Local = Local(this)
fun Local.asLocalVar(startIndex: Local = Local.UNKNOWN, endIndex: Local = Local.UNKNOWN): LocalVarIndex =
    LocalVarIndex(this, startIndex, endIndex)
fun Int.asLocal(startIndex: Local = Local.UNKNOWN, endIndex: Local = Local.UNKNOWN): LocalVarIndex =
    this.asLocal().asLocalVar(startIndex, endIndex)
fun Local.asLvtIndex(): LvtIndex = LvtIndex(this)
fun Int.asLvtIndex(): LvtIndex = this.asLocal().asLvtIndex()

data class LinePriority(val coord: Int, val priority: Int)

private const val BASE_PRIORITY_VALUE = 0
private const val HIGH_PRIORITY_VALUE = 100
private const val LOW_PRIORITY_VALUE = -100

val Int.basePriority: LinePriority
    get() = priority(BASE_PRIORITY_VALUE)
val LinePriority.basePriority: LinePriority
    get() = if (priority == BASE_PRIORITY_VALUE) {
        this
    } else {
        coord.basePriority
    }

val Int.highPriority: LinePriority
    get() = priority(HIGH_PRIORITY_VALUE)
val LinePriority.highPriority: LinePriority
    get() = if (priority == HIGH_PRIORITY_VALUE) {
        this
    } else {
        coord.highPriority
    }

val Int.lowPriority: LinePriority
    get() = priority(LOW_PRIORITY_VALUE)
val LinePriority.lowPriority: LinePriority
    get() = if (priority == LOW_PRIORITY_VALUE) {
        this
    } else {
        coord.lowPriority
    }

fun Int.priority(priority: Int) = LinePriority(this, priority)

class ParamMap<T>(
    private val backing: Int2ObjectLinkedOpenHashMap<T> = Int2ObjectLinkedOpenHashMap<T>()
) : MutableMap<Int, T> by backing {
    @Deprecated(message = "Use ParameterRef to access", replaceWith = ReplaceWith("this.get(key.asParam())"))
    override fun get(key: Int): T? {
        return backing.get(key)
    }

    @Deprecated(message = "Use ParameterRef to access", replaceWith = ReplaceWith("this.put(key.asParam(), value)"))
    override fun put(key: Int, value: T): T? {
        return backing.put(key, value)
    }
    @Deprecated(message = "Use ParameterRef to access", replaceWith = ReplaceWith("this.set(key.asParam(), value)"))
    operator fun set(key: Int, value: T): T? {
        return backing.put(key, value)
    }

    operator fun get(key: ParamIndex): T? = backing.get(key.index)
    operator fun set(key: ParamIndex, value: T) {
        backing.put(key.index, value)
    }
}