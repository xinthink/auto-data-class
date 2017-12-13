package com.fivemiles.auto.dataclass

import com.google.auto.common.MoreElements.isAnnotationPresent
import com.squareup.kotlinpoet.*
import org.jetbrains.annotations.Nullable
import javax.lang.model.element.Element
import javax.lang.model.element.ExecutableElement
import javax.lang.model.element.Modifier


/**
 * Kotlin equivalents of some Java types.
 */
private val javaTypeMappings: Map<TypeName, ClassName> = mapOf(
        List::class.java.asClassName() to List::class.asClassName(),
        Set::class.java.asClassName() to Set::class.asClassName(),
        Map::class.java.asClassName() to Map::class.asClassName(),
        HashMap::class.java.asClassName() to Map::class.asClassName(),
        Byte::class.java.asTypeName() to BYTE,
        java.lang.Byte::class.java.asTypeName() to BYTE,
        Short::class.java.asTypeName() to SHORT,
        java.lang.Short::class.java.asTypeName() to SHORT,
        Integer::class.java.asTypeName() to INT,
        Long::class.java.asTypeName() to LONG,
        java.lang.Long::class.java.asTypeName() to LONG,
        Double::class.java.asTypeName() to DOUBLE,
        java.lang.Double::class.java.asTypeName() to DOUBLE,
        Float::class.java.asTypeName() to FLOAT,
        java.lang.Float::class.java.asTypeName() to FLOAT,
        Boolean::class.java.asTypeName() to BOOLEAN,
        java.lang.Boolean::class.java.asTypeName() to BOOLEAN,
        Char::class.java.asTypeName() to CHAR,
        java.lang.Character::class.java.asTypeName() to CHAR,
        String::class.java.asTypeName() to String::class.asClassName(),
        Any::class.java.asTypeName() to ANY
)

/**
 * Parse property type (in Kotlin) from the getter method.
 */
fun parsePropertyType(propertyMethod: ExecutableElement): TypeName {
    val type = mapToKotlinType(propertyMethod.returnType.asTypeName())
    return if (isAnnotationPresent(propertyMethod, Nullable::class.java))
        type.asNullable() else type
}

/**
 * Map `type` parsed from bytecode to Kotlin types, if necessary
 */
private fun mapToKotlinType(type: TypeName): TypeName {
    if (type !is ParameterizedTypeName) return javaTypeMappings[type] ?: type

    val ktTypeName = javaTypeMappings[type.rawType]
    val rawTypeName = ktTypeName ?: type.rawType
    return ParameterizedTypeName.get(rawTypeName, *type.typeArguments
            .map(::mapToKotlinType)
            .toTypedArray())
}

val Element.isInterfaceOrAbstractClass: Boolean
        get() = kind.isInterface || (kind.isClass && Modifier.ABSTRACT in modifiers)

val Element.isNestedType: Boolean
        get() = enclosingElement.kind.let { it.isInterface || it.isClass }
