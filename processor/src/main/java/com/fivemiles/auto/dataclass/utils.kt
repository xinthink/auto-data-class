package com.fivemiles.auto.dataclass

import com.google.auto.common.MoreElements.isAnnotationPresent
import com.squareup.kotlinpoet.*
import org.jetbrains.annotations.Nullable
import javax.lang.model.element.ExecutableElement


/**
 * Kotlin equivalents of some Java types.
 */
private val javaTypeMappings: Map<TypeName, ClassName> = mapOf(
        List::class.java.asClassName() to List::class.asClassName(),
        Set::class.java.asClassName() to Set::class.asClassName(),
        Map::class.java.asClassName() to Map::class.asClassName(),
        HashMap::class.java.asClassName() to Map::class.asClassName(),
        Byte::class.java.asTypeName() to Byte::class.asClassName(),
        Short::class.java.asTypeName() to Short::class.asClassName(),
        Integer::class.java.asTypeName() to Int::class.asClassName(),
        Double::class.java.asTypeName() to Double::class.asClassName(),
        Float::class.java.asTypeName() to Float::class.asClassName(),
        Boolean::class.java.asTypeName() to Boolean::class.asClassName(),
        Char::class.java.asTypeName() to Char::class.asClassName(),
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
