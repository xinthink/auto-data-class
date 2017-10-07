package com.fivemiles.auto.dataclass

import android.os.Bundle
import android.os.Parcel
import android.os.Parcelable
import android.text.TextUtils
import com.google.auto.common.MoreElements
import com.squareup.kotlinpoet.*
import org.jetbrains.annotations.Nullable
import java.io.Serializable
import javax.annotation.processing.ProcessingEnvironment
import javax.lang.model.element.ExecutableElement

/**
 * Generates [Parcelable] stuff for the data class
 *
 * Created by ywu on 2017/8/24.
 */
internal class ParcelableGenerator(
        processingEnv: ProcessingEnvironment,
        private val errorReporter: ErrorReporter) : FacetGenerator {

    private val typeUtils = processingEnv.typeUtils
    private val elementUtils = processingEnv.elementUtils

    override fun applicable(dataClassDef: DataClassDef): Boolean {
        val parcelableType = elementUtils.getTypeElement(Parcelable::class.qualifiedName)?.asType()
        val dataClassType = dataClassDef.element.asType()
        return parcelableType != null && typeUtils.isAssignable(dataClassType, parcelableType)
    }

    override fun generate(dataClassDef: DataClassDef,
                          propertyMethods: Map<String, ExecutableElement>,
                          dataClassSpecBuilder: TypeSpec.Builder) {
        val concreteClassName = dataClassDef.className
        val creatorClsType = ParameterizedTypeName.get(Parcelable.Creator::class.asClassName(),
                concreteClassName)

        dataClassSpecBuilder.companionObject(TypeSpec.companionObjectBuilder().addProperty(
                PropertySpec.builder(PARCELABLE_CREATOR_NAME, creatorClsType)
                        .initializer("%L", generateCreator(dataClassDef, creatorClsType, propertyMethods))
                        .build()).build())
    }

    private fun generateCreator(dataClassDef: DataClassDef,
                                creatorType: TypeName,
                                propertyMethods: Map<String, ExecutableElement>): TypeSpec {
        return TypeSpec.objectBuilder("")
                .addSuperinterface(creatorType)
                .addFun(fromParcelFunSpec(dataClassDef, propertyMethods))
                .addFun(FunSpec.builder("newArray")
                        .addModifiers(KModifier.OVERRIDE)
                        .addParameter("size", Int::class)
                        .returns(ParameterizedTypeName.get(Array<Any>::class.asClassName(),
                                dataClassDef.className.asNullable()))
                        .addCode("return arrayOfNulls(size)\n")
                        .build())
                .build()
    }

    /** implements the [Parcelable.Creator.createFromParcel] method */
    private fun fromParcelFunSpec(dataClassDef: DataClassDef,
                                  propertyMethods: Map<String, ExecutableElement>): FunSpec {
        val paramSource = "source"
        return FunSpec.builder("createFromParcel")
                .addModifiers(KModifier.OVERRIDE)
                .addParameter(paramSource, Parcel::class)
                .returns(dataClassDef.className)
                .addCode("return %T(\n%L\n)", dataClassDef.className, CodeBlock.builder().apply {
                    propertyMethods.entries.forEachIndexed { i, (_, m) ->
                        val propType = propertyType(m)
                        val nonNullableType = propType.asNonNullable()
                        if (propType.nullable) {
                            add("if ($paramSource.readByte() == 0.toByte()) ")
                                    .readValue(paramSource, nonNullableType)
                                    .add(" else null")
                        } else {
                            readValue(paramSource, nonNullableType)
                        }

                        if (i < propertyMethods.size - 1) {
                            add(",\n")
                        }
                    }
                }.build())
                .build()
    }

    private fun propertyType(propertyMethod: ExecutableElement): TypeName {
        val type = propertyMethod.returnType.asTypeName()
        return if (MoreElements.isAnnotationPresent(propertyMethod, Nullable::class.java)) type.asNullable() else type
    }

    private fun parcelableType(originType: TypeName): TypeName =
            (originType as? ParameterizedTypeName)?.rawType ?: originType

    private fun CodeBlock.Builder.readValue(sourceParam: String, type: TypeName): CodeBlock.Builder {
        val parcelableType = parcelableType(type)

        fun CodeBlock.Builder.addSimpleRead(strType: String) {
            add("%L.read%L()", sourceParam, strType)
        }

        fun CodeBlock.Builder.addExplicitRead(strType: String, useClassLoader: Boolean = true) {
            if (useClassLoader)
                add("%L.read%L(%T::class.java.classLoader) as %T", sourceParam, strType, type, type)
            else
                add("%L.read%L() as %T", sourceParam, strType, type)
        }

        when (parcelableType) {
            BYTE -> addSimpleRead("Byte")
            INT -> addSimpleRead("Int")
            SHORT -> addExplicitRead("Int", false)
            CHAR -> addExplicitRead("Int", false)
            LONG -> addSimpleRead("Long")
            FLOAT -> addSimpleRead("Float")
            DOUBLE -> addSimpleRead("Double")
            BOOLEAN -> add("%L.readByte() == 1.toByte()", sourceParam)
            STRING, KT_STRING -> addSimpleRead("String")
            LIST, KT_LIST -> addExplicitRead("ArrayList")
            MAP, KT_MAP -> addExplicitRead("HashMap")
            CHARSEQUENCE, KT_CHARSEQUENCE ->
                add("%T.CHAR_SEQUENCE_CREATOR.createFromParcel(%L)", TextUtils::class, sourceParam)
            BUNDLE -> addExplicitRead("Bundle")
            PARCELABLE -> addExplicitRead("Parcelable")
            else -> addExplicitRead("Value")
        }
        return this
    }

    companion object {
        private const val PARCELABLE_CREATOR_NAME = "CREATOR"

        val STRING = String::class.java.asClassName()
        val KT_STRING = String::class.asClassName()
        val MAP = Map::class.java.asClassName()
        val KT_MAP = Map::class.asClassName()
        val LIST = List::class.java.asClassName()
        val KT_LIST = List::class.asClassName()
        val BOOLEANARRAY = ParameterizedTypeName.get(Array<Any>::class, Boolean::class)
        val BYTEARRAY = ParameterizedTypeName.get(Array<Any>::class, Byte::class)
        val CHARARRAY = ParameterizedTypeName.get(Array<Any>::class, Char::class)
        val INTARRAY = ParameterizedTypeName.get(Array<Any>::class, Int::class)
        val LONGARRAY = ParameterizedTypeName.get(Array<Any>::class, Long::class)
        val STRINGARRAY = ParameterizedTypeName.get(Array<Any>::class.asClassName(), STRING)
        val KT_STRINGARRAY = ParameterizedTypeName.get(Array<Any>::class, String::class)
//        val SPARSEARRAY = ClassName.get("android.util", "SparseArray")
//        val SPARSEBOOLEANARRAY = ClassName.get("android.util", "SparseBooleanArray")
        val BUNDLE = Bundle::class.asClassName()
//        val PERSISTABLEBUNDLE = ClassName.get("android.os", "PersistableBundle")
        val PARCELABLE = Parcelable::class.asClassName()
        val PARCELABLEARRAY = ParameterizedTypeName.get(Array<Any>::class, Parcelable::class)
        val CHARSEQUENCE = CharSequence::class.java.asClassName()
        val KT_CHARSEQUENCE = CharSequence::class.asClassName()
//        val IBINDER = ClassName.get("android.os", "IBinder")
//        val OBJECTARRAY = ArrayTypeName.of(TypeName.OBJECT)
        val SERIALIZABLE = Serializable::class.asClassName()
//        val SIZE = ClassName.get("android.util", "Size")
//        val SIZEF = ClassName.get("android.util", "SizeF")
//        val TEXTUTILS = ClassName.get("android.text", "TextUtils")
//        val ENUM = ClassName.get(Enum<*>::class.java)
//        val IMMUTABLE_LIST = ClassName.get("com.google.common.collect", "ImmutableList")
//        val IMMUTABLE_SET = ClassName.get("com.google.common.collect", "ImmutableSet")
//        val IMMUTABLE_MAP = ClassName.get("com.google.common.collect", "ImmutableMap")
    }
}
