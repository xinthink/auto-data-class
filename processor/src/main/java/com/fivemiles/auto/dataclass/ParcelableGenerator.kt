package com.fivemiles.auto.dataclass

import android.os.Parcel
import android.os.Parcelable
import android.text.TextUtils
import com.fivemiles.auto.dataclass.parcel.ParcelAdapter
import com.google.auto.common.AnnotationMirrors.getAnnotationValue
import com.squareup.kotlinpoet.*
import java.io.Serializable
import java.util.*
import javax.annotation.processing.ProcessingEnvironment
import javax.lang.model.type.TypeMirror

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

    private val parcelableTypeMirror: TypeMirror by lazy {
        elementUtils.getTypeElement(PARCELABLE.canonicalName).asType()
    }

    override fun isApplicable(dataClassDef: DataClassDef): Boolean {
        val parcelableType = elementUtils.getTypeElement(Parcelable::class.qualifiedName)?.asType()
        val dataClassType = dataClassDef.element.asType()
        return parcelableType != null && typeUtils.isAssignable(dataClassType, parcelableType)
    }

    override fun generate(dataClassDef: DataClassDef,
                          dataClassSpecBuilder: TypeSpec.Builder) {
        dataClassSpecBuilder
                .companionObject(TypeSpec.companionObjectBuilder()
                        .addParcelableCreator(dataClassDef, dataClassDef.persistentProperties)
                        .addCustomAdapters(dataClassDef.persistentProperties)
                        .build())
                .describeContentsFunSpec()
                .writeParcelFunSpec(dataClassDef.persistentProperties)
    }

    private fun TypeSpec.Builder.addParcelableCreator(
            dataClassDef: DataClassDef,
            properties: Set<DataPropDef>
    ): TypeSpec.Builder {
        val concreteClassName = dataClassDef.className
        val creatorClsType = ParameterizedTypeName.get(Parcelable.Creator::class.asClassName(), concreteClassName)
        return addProperty(PropertySpec.builder(PARCELABLE_CREATOR_NAME, creatorClsType)
                .addAnnotation(JvmStatic::class)
                .initializer("%L", generateCreator(dataClassDef, creatorClsType, properties))
                .build())
    }

    /** implements [Parcelable.Creator] */
    private fun generateCreator(dataClassDef: DataClassDef,
                                creatorType: TypeName,
                                properties: Set<DataPropDef>): TypeSpec {
        return TypeSpec.objectBuilder("")
                .addSuperinterface(creatorType)
                .addFunction(fromParcelFunSpec(dataClassDef, properties))
                .addFunction(FunSpec.builder("newArray")
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
                                  properties: Set<DataPropDef>): FunSpec {
        val paramSource = "source"
        return FunSpec.builder("createFromParcel")
                .addModifiers(KModifier.OVERRIDE)
                .addParameter(paramSource, Parcel::class)
                .returns(dataClassDef.className)
                .addCode("return %T(\n%L\n)", dataClassDef.className, CodeBlock.builder().apply {
                    properties.forEachIndexed { i, prop ->
                        val propType = prop.typeKt
                        if (propType.nullable) {
                            add("if ($paramSource.readByte() == 0.toByte()) ")
                                    .readValue(paramSource, prop)
                                    .add(" else null")
                        } else {
                            readValue(paramSource, prop)
                        }

                        if (i < properties.size - 1) {
                            add(",\n")
                        }
                    }
                }.build())
                .build()
    }

    private fun parcelableType(prop: DataPropDef): TypeName =
            if (isParcelable(prop)) PARCELABLE
            else {
                val nonNullType = prop.typeKt.asNonNullable()
                (nonNullType as? ParameterizedTypeName)?.rawType ?: nonNullType
            }

    private fun isParcelable(prop: DataPropDef): Boolean =
            typeUtils.isAssignable(prop.typeMirror, parcelableTypeMirror)

    private fun CodeBlock.Builder.readValue(sourceParam: String, prop: DataPropDef): CodeBlock.Builder {
        val rawType = parcelableType(prop)
        val nonNullType = prop.typeKt.asNonNullable()

        fun CodeBlock.Builder.addSimpleRead(serializedType: String,
                                            dataType: String = serializedType) {
            add("%L.read%L()", sourceParam, serializedType)
            if (serializedType != dataType) add(".to%L()", dataType)
        }

        fun CodeBlock.Builder.addExplicitRead(serializedType: String,
                                              dataType: String = serializedType,
                                              useClassLoader: Boolean = true) {
            if (useClassLoader)
                add("%L.read%L(%T::class.java.classLoader)", sourceParam, serializedType, rawType)
            else add("%L.read%L()", sourceParam, serializedType)

            if (serializedType != dataType) add(".to%L()", dataType)
            add(" as %T", nonNullType)
        }

        when (rawType) {
            BYTE -> addSimpleRead("Byte")
            INT -> addSimpleRead("Int")
            SHORT -> addSimpleRead("Int", "Short")
            CHAR -> addSimpleRead("Int", "Char")
            LONG -> addSimpleRead("Long")
            FLOAT -> addSimpleRead("Float")
            DOUBLE -> addSimpleRead("Double")
            BOOLEAN -> add("%L.readByte() == 1.toByte()", sourceParam)
            STRING, KT_STRING -> addSimpleRead("String")
            LIST, KT_LIST -> addExplicitRead("ArrayList")
            SET, KT_SET -> addExplicitRead("ArrayList", "Set")
            MAP, KT_MAP -> addExplicitRead("HashMap")
            CHARSEQUENCE, KT_CHARSEQUENCE ->
                add("%T.CHAR_SEQUENCE_CREATOR.createFromParcel(%L)", TextUtils::class, sourceParam)
//            BUNDLE -> addExplicitRead("Bundle")
            PARCELABLE -> add("%L.readParcelable(%T::class.java.classLoader)", sourceParam, nonNullType)
            else -> addExplicitRead("Value")
        }
        return this
    }

    /** Custom [ParcelAdapter]s, if any, defined as companion object properties */
    private fun TypeSpec.Builder.addCustomAdapters(
            properties: Set<DataPropDef>
    ): TypeSpec.Builder {
        properties.forEach {
            val propDef = it.dataPropAnnotation
            val customAdapterType = if (propDef != null) {
                val annotatedAdapterType = (getAnnotationValue(propDef,
                        DataProp::parcelAdapter.name).value as TypeMirror).asTypeName()
                if (ParcelAdapter::class.asTypeName() != annotatedAdapterType)
                    annotatedAdapterType else null
            } else null

            if (customAdapterType != null) {
                addProperty(PropertySpec.builder(customAdapterPropName(it.name), customAdapterType)
                        .addModifiers(KModifier.PRIVATE)
                        .delegate("lazy { %T() }", customAdapterType)
                        .build())
            }
        }
        return this
    }

    private fun customAdapterPropName(p: String) = "${p}_ADAPTER".toUpperCase(Locale.US)

    /** implements the [Parcelable.describeContents] method */
    private fun TypeSpec.Builder.describeContentsFunSpec(): TypeSpec.Builder =
            addFunction(FunSpec.builder("describeContents")
                    .addModifiers(KModifier.OVERRIDE)
                    .returns(Int::class)
                    .addCode("return 0")
                    .build())

    /** implements the [Parcelable.writeToParcel] method */
    private fun TypeSpec.Builder.writeParcelFunSpec(
            properties: Set<DataPropDef>
    ): TypeSpec.Builder {
        val paramDest = "dest"
        val paramFlags = "flags"
        return addFunction(FunSpec.builder("writeToParcel")
                .addModifiers(KModifier.OVERRIDE)
                .addParameter(paramDest, Parcel::class)
                .addParameter(paramFlags, Int::class)
                .addCode(CodeBlock.builder()
                        .writeParcelFunBody(paramDest, paramFlags, properties)
                        .build()
                )
                .build())
    }

    private fun CodeBlock.Builder.writeParcelFunBody(
            paramDest: String,
            paramFlags: String,
            properties: Set<DataPropDef>
    ): CodeBlock.Builder {
        properties.forEach {
            val p = it.name
            val propType = it.typeKt

            if (propType.nullable) {
                beginControlFlow("if ($p == null)")
                addStatement("$paramDest.writeByte(1.toByte())")
                nextControlFlow("else")
                addStatement("$paramDest.writeByte(0.toByte())")
                writeValue(paramDest, paramFlags, it)
                endControlFlow()
            } else {
                writeValue(paramDest, paramFlags, it)
            }
        }
        return this
    }

    private fun CodeBlock.Builder.writeValue(paramDest: String,
                                             paramFlags: String,
                                             prop: DataPropDef
    ): CodeBlock.Builder {
        val name = prop.name
        val attr = prop.dataPropAnnotation

        // prefer custom TypeAdapter if any
        val customAdapterType = if (attr != null) {
            val annotatedAdapterType = (getAnnotationValue(attr,
                    DataProp::parcelAdapter.name).value as TypeMirror).asTypeName()
            if (ParcelAdapter::class.asTypeName() != annotatedAdapterType)
                annotatedAdapterType else null
        } else null

        if (customAdapterType != null) {
            add("%L.toParcel(%L, %L)\n", customAdapterPropName(name), name, paramDest)
            return this
        }

        val rawType = parcelableType(prop)
        fun addSimpleWrite(serializedType: String) {
            add("%L.write%L(%L)\n", paramDest, serializedType, name)

        }
        fun addExplicitWrite(serializedType: String) {
            add("%L.write%L(%L.to%L())\n", paramDest, serializedType, name, serializedType)
        }

        when (rawType) {
            SHORT,
            CHAR -> addExplicitWrite("Int")
            BOOLEAN -> add("%L.writeByte((if (%L) 1 else 0).toByte())\n", paramDest, name)
            LIST, KT_LIST -> addSimpleWrite("List")
            SET, KT_SET -> addExplicitWrite("List")
            MAP, KT_MAP -> addSimpleWrite("Map")
            PARCELABLE -> add("$paramDest.writeParcelable($name, $paramFlags)\n")
            else -> add("%L.write%T(%L)\n", paramDest, rawType, name)
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
        val SET = Set::class.java.asClassName()
        val KT_SET = Set::class.asClassName()
//        val BOOLEANARRAY = ParameterizedTypeName.get(Array<Any>::class, Boolean::class)
//        val BYTEARRAY = ParameterizedTypeName.get(Array<Any>::class, Byte::class)
//        val CHARARRAY = ParameterizedTypeName.get(Array<Any>::class, Char::class)
//        val INTARRAY = ParameterizedTypeName.get(Array<Any>::class, Int::class)
//        val LONGARRAY = ParameterizedTypeName.get(Array<Any>::class, Long::class)
//        val STRINGARRAY = ParameterizedTypeName.get(Array<Any>::class.asClassName(), STRING)
//        val KT_STRINGARRAY = ParameterizedTypeName.get(Array<Any>::class, String::class)
//        val SPARSEARRAY = ClassName.get("android.util", "SparseArray")
//        val SPARSEBOOLEANARRAY = ClassName.get("android.util", "SparseBooleanArray")
//        val BUNDLE = Bundle::class.asClassName()
//        val PERSISTABLEBUNDLE = ClassName.get("android.os", "PersistableBundle")
        val PARCELABLE = Parcelable::class.asClassName()
//        val PARCELABLEARRAY = ParameterizedTypeName.get(Array<Any>::class, Parcelable::class)
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
