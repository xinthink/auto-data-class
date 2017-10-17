package com.fivemiles.auto.dataclass

import com.google.auto.common.AnnotationMirrors.getAnnotationValue
import com.google.gson.Gson
import com.google.gson.TypeAdapter
import com.google.gson.reflect.TypeToken
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonToken
import com.google.gson.stream.JsonWriter
import com.squareup.kotlinpoet.*
import javax.annotation.processing.ProcessingEnvironment
import javax.lang.model.element.TypeElement
import javax.lang.model.type.TypeMirror

/**
 * Generates [Gson TypeAdapter][TypeAdapter] for the data class
 *
 * Created by ywu on 2017/7/10.
 */
internal class GsonTypeAdapterGenerator(
        processingEnv: ProcessingEnvironment,
        private val errorReporter: ErrorReporter) : FacetGenerator {

    private val typeUtils = processingEnv.typeUtils
    private val elementUtils = processingEnv.elementUtils

    private lateinit var concreteDataClassSimpleName: String

    override fun applicable(dataClassDef: DataClassDef): Boolean {
        val adc = dataClassDef.element.getAnnotation(DataClass::class.java)
        return adc.generateGsonTypeAdapter
    }

    override fun generate(dataClassDef: DataClassDef,
                          dataClassSpecBuilder: TypeSpec.Builder) {
        concreteDataClassSimpleName = dataClassDef.className.simpleName()
        val interfaceElement = dataClassDef.element
        val adapterClsName = GSON_ADAPTER_CLASS_NAME  // nested class name
        val superClsTypeName = ParameterizedTypeName.Companion.get(TypeAdapter::class.asClassName(),
                interfaceElement.asClassName())
        val adapterClsSpec = TypeSpec.classBuilder(adapterClsName)
                .superclass(superClsTypeName)
                .primaryConstructor(FunSpec.constructorBuilder()
                        .addParameter("gson", Gson::class)
                        .build())
                .addProperties(dataClassDef.persistentProperties.flatMap(this::propDefaultAndAdapter))
                .addFunction(gsonReaderFunSpec(interfaceElement, dataClassDef.persistentProperties))
                .addFunction(gsonWriterFunSpec(interfaceElement, dataClassDef.persistentProperties))
                .build()
        dataClassSpecBuilder.addType(adapterClsSpec)
    }

    /** define default value / typeAdapter for each property */
    private fun propDefaultAndAdapter(propDef: DataPropDef): List<PropertySpec> {
        val p = propDef.name
        val propType = propDef.typeKt
        val defMirror = propDef.dataPropAnnotation

        // default value
        val defaultValueProp = PropertySpec.builder("default${p.capitalize()}", propType.asNullable())
                .mutable(true)
                .apply {
                    val defaultValue = propDef.defaultValueLiteral
                    initializer(if (defaultValue.isNotBlank()) defaultValue else "null")
                }
                .build()

        // typeAdapter
        val nonNullPropType = propType.asNonNullable()
        val adapterType = ParameterizedTypeName.get(TypeAdapter::class.asClassName(), nonNullPropType)
        val typeAdapterProp = PropertySpec.builder("${p}Adapter", adapterType)
                .addModifiers(KModifier.PRIVATE)
                .apply {
                    // Get custom TypeAdapter if any
                    val customAdapterType = if (defMirror != null) {
                        val annotatedAdapterType = (getAnnotationValue(defMirror,
                                DataProp::gsonTypeAdapter.name).value as TypeMirror).asTypeName()
                        if (TypeAdapter::class.asTypeName() != annotatedAdapterType)
                            annotatedAdapterType else null
                    } else null

                    // TypeAdapter delegate
                    if (customAdapterType != null) {
                        delegate("lazy { %T() }", customAdapterType)
                    } else {
                        val adapteeTypeBlock = dumpTypeToken(nonNullPropType)
                        if (nonNullPropType is ParameterizedTypeName)
                            delegate("lazy { gson.getAdapter(%L) as %T }", adapteeTypeBlock, adapterType)
                        else delegate("lazy { gson.getAdapter(%L) }", adapteeTypeBlock)
                    }
                }
                .build()

        return listOf(defaultValueProp, typeAdapterProp)
    }

    /** implements the [TypeAdapter.read] method */
    private fun gsonReaderFunSpec(type: TypeElement, properties: Set<DataPropDef>): FunSpec {
        val paramJsonReader = "jsonReader"
        return FunSpec.builder("read")
                .addModifiers(KModifier.OVERRIDE)
                .addParameter(paramJsonReader, JsonReader::class)
                .returns(type.asClassName().asNullable())
                .beginControlFlow("if ($paramJsonReader.peek() == %T.NULL)", JsonToken::class)
                .addStatement("$paramJsonReader.nextNull()")
                .addStatement("return null")
                .endControlFlow()
                .addStatement("%W")
                .apply {
                    // define local variables, initialized with default values
                    properties.forEach {
                        val p = it.name
                        val propType = it.typeKt
                        val nullablePropType = propType.asNullable()

                        addStatement("var $p: %T = default${p.capitalize()}", nullablePropType)
                    }

                    // read json stream
                    addStatement("%W")
                    addStatement("$paramJsonReader.beginObject()")
                    beginControlFlow("while ($paramJsonReader.hasNext())")
                    beginControlFlow("if ($paramJsonReader.peek() == %T.NULL)", JsonToken::class)
                    addStatement("$paramJsonReader.nextNull()")
                    addStatement("continue")
                    endControlFlow()

                    beginControlFlow("when ($paramJsonReader.nextName())")
                    properties.forEach { generatePropertyReader(paramJsonReader, it) }
                    addStatement("else -> $paramJsonReader.skipValue()")
                    endControlFlow()
                    endControlFlow()
                    addStatement("$paramJsonReader.endObject()\n")

                    // null safety check
                    properties
                            .filter { !it.typeKt.nullable }
                            .forEach {
                                beginControlFlow("if (${it.name} == null)")
                                addStatement("throw IllegalArgumentException(%S)", "${it.name} must not be null!")
                                endControlFlow()
                            }

                    addStatement("return $concreteDataClassSimpleName(%L)",
                            properties.map(DataPropDef::name).joinToString())
                }
                .build()
    }

    private fun FunSpec.Builder.generatePropertyReader(paramJsonReader: String,
                                                       propDef: DataPropDef) {
        val propDefMirror = propDef.dataPropAnnotation
        val propName = propDef.name

        // preferred json field name
        val jsonFieldName: String = if (propDefMirror != null) {
            val _fieldName = getAnnotationValue(propDefMirror,
                    DataProp::jsonField.name).value as String
            if (_fieldName.isNotBlank()) _fieldName else propName
        } else propName

        addStatement("%S -> %L = %LAdapter.read(%L)", jsonFieldName, propName, propName, paramJsonReader)

        // alternatives if any
        if (propDefMirror != null) {
            @Suppress("UNCHECKED_CAST")
            val alternativeNames = getAnnotationValue(propDefMirror,
                    DataProp::jsonFieldAlternate.name).value as List<Any>
            if (alternativeNames.size > 1) {
                addStatement("in arrayOf(%L) -> $propName = ${propName}Adapter.read($paramJsonReader)", alternativeNames)
            } else if (alternativeNames.isNotEmpty()) {
                addStatement("%L -> $propName = ${propName}Adapter.read($paramJsonReader)", alternativeNames)
            }
        }
    }

    /** implements the [TypeAdapter.write] method */
    private fun gsonWriterFunSpec(type: TypeElement, properties: Set<DataPropDef>): FunSpec {
        val paramNameWriter = "jsonWriter"
        val paramNameObj = "value"

        return FunSpec.builder("write")
                .addModifiers(KModifier.OVERRIDE)
                .addParameter(paramNameWriter, JsonWriter::class)
                .addParameter(paramNameObj, type.asClassName().asNullable())
                .beginControlFlow("if ($paramNameObj == null)")
                .addStatement("$paramNameWriter.nullValue()")
                .addStatement("return")
                .endControlFlow()
                .addStatement("$paramNameWriter.beginObject()")
                .apply {
                    properties.forEach {
                        // preferred json field name
                        val p = it.name
                        val propDefMirror = it.dataPropAnnotation
                        val jsonFieldName: String = if (propDefMirror != null) {
                            val _fieldName = getAnnotationValue(propDefMirror,
                                    DataProp::jsonField.name).value as String
                            if (_fieldName.isNotBlank()) _fieldName else p
                        } else p

                        addStatement("$paramNameWriter.name(%S)", jsonFieldName)
                        addStatement("${p}Adapter.write($paramNameWriter, $paramNameObj.$p)")
                    }
                }
                .addStatement("$paramNameWriter.endObject()")
                .build()
    }

    private fun dumpTypeToken(propType: TypeName,
                              asType: Boolean = false  // produce a `Type` instead of a `TypeToken`
    ) = CodeBlock.builder()
            .apply {
                if (propType is ParameterizedTypeName)
                    add(dumpParameterizedTypeToken(propType, asType))
                else add("%T::class.%L", propType, javaTypeAccessor(propType, asType))
            }
            .build()

    /**
     * Choose the right accessor for java types mapping.
     * Avoid primitives for arguments of parameterized types.
     */
    private fun javaTypeAccessor(type: TypeName, preferObjectType: Boolean = false): String =
            when (type) {
                BYTE, CHAR, SHORT, INT, LONG, FLOAT, DOUBLE, BOOLEAN ->
                    if (preferObjectType) "javaObjectType" else "java"
                else -> "java"
            }

    private fun dumpParameterizedTypeToken(propType: ParameterizedTypeName,
                                           asType: Boolean = false
    ) = CodeBlock.builder()
            .addParameterizedType(propType, asType)
            .build()

    private fun CodeBlock.Builder.addParameterizedType(propType: ParameterizedTypeName,
                                                       asType: Boolean = false  // produce a `Type` instead of a `TypeToken`
    ): CodeBlock.Builder =
            add("%T.getParameterized(%T::class.java", TypeToken::class, propType.rawType)
                    .apply {
                        propType.typeArguments.forEach {
                            add(", ")
                            add(dumpTypeToken(it, asType = true))
                        }
                    }
                    .add(")${if (asType) ".type" else ""}")  // when used as a type variable, the `type` property should be returned

    companion object {
        private const val GSON_ADAPTER_CLASS_NAME = "GsonTypeAdapter"
    }
}
