package com.fivemiles.auto.dataclass

import com.google.auto.common.AnnotationMirrors.getAnnotationValue
import com.google.auto.common.MoreElements
import com.google.auto.common.MoreElements.getAnnotationMirror
import com.google.gson.Gson
import com.google.gson.TypeAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonWriter
import com.squareup.kotlinpoet.*
import org.jetbrains.annotations.Nullable
import javax.annotation.processing.ProcessingEnvironment
import javax.lang.model.element.ExecutableElement
import javax.lang.model.element.TypeElement

/**
 * Generates [Gson TypeAdapter][TypeAdapter] for the data class
 *
 * Created by ywu on 2017/7/10.
 */
internal class GsonTypeAdapterGenerator(
        processingEnv: ProcessingEnvironment,
        val errorReporter: ErrorReporter) {

    private val typeUtils = processingEnv.typeUtils
    private val elementUtils = processingEnv.elementUtils

    private lateinit var concreteDataClassSimpleName: String

    /**
     * Generate TypeAdapter as a nested class of the given data class
     *
     * @param interfaceElement the interface element which defines the data class
     * @param propertyMethods getter methods define the properties of the data class
     * @param dataClassSpecBuilder [builder instance][TypeSpec.Builder] to build the data class
     */
    fun generate(interfaceElement: TypeElement,
                 propertyMethods: Map<String, ExecutableElement>,
                 dataClassSpecBuilder: TypeSpec.Builder) {
        concreteDataClassSimpleName = dataClassSpecBuilder.simpleName
        val adapterClsName = GSON_ADAPTER_CLASS_NAME  // nested class name
        val superClsTypeName = ParameterizedTypeName.Companion.get(TypeAdapter::class.asClassName(),
                interfaceElement.asClassName())
        val adapterClsSpec = TypeSpec.classBuilder(adapterClsName)
                .superclass(superClsTypeName)
                .primaryConstructor(FunSpec.constructorBuilder()
                        .addParameter("gson", Gson::class)
                        .build())
                .addProperties(propertyMethods.flatMap { (p, m) -> propDefaultAndAdapter(p, m) })
                .addFun(gsonReaderFunSpec(interfaceElement, propertyMethods))
                .addFun(gsonWriterFunSpec(interfaceElement, propertyMethods))
                .build()
        dataClassSpecBuilder.addType(adapterClsSpec)
    }

    /** define default value / typeAdapter for each property */
    private fun propDefaultAndAdapter(p: String, m: ExecutableElement): List<PropertySpec> {
        val propDefMirror = getAnnotationMirror(m, DataClassProp::class.java).orNull()
        val propType = propertyType(m)

        // default value
        val defaultValueProp = PropertySpec.builder("default${p.capitalize()}", propType.asNullable())
                .mutable(true)
                .apply {
                    if (propDefMirror != null) {
                        val defaultValue = getAnnotationValue(propDefMirror,
                                DataClassProp::defaultValueLiteral.name).value as String
                        initializer(if (defaultValue.isNotBlank()) defaultValue else "null")
                    } else {
                        initializer("null")
                    }
                }
                .build()

        // typeAdapter
        val nonNullPropType = propType.asNonNullable()
        val typeAdapterProp = PropertySpec.builder("${p}Adapter",
                ParameterizedTypeName.get(
                        TypeAdapter::class.asClassName(), nonNullPropType))
                .addModifiers(KModifier.PRIVATE)
                .initializer("gson.getAdapter(%T::class.java)", nonNullPropType)
                .build()

        return listOf(defaultValueProp, typeAdapterProp)
    }

    /** implements the [TypeAdapter.read] method */
    private fun gsonReaderFunSpec(type: TypeElement, propertyMethods: Map<String, ExecutableElement>): FunSpec {
        val paramNameReader = "jsonReader"
        return FunSpec.builder("read")
                .addModifiers(KModifier.OVERRIDE)
                .addParameter(paramNameReader, JsonReader::class)
                .returns(type.asClassName().asNullable())
                .beginControlFlow("if ($paramNameReader.peek() == $QN_GSON_TOKEN_NULL)")
                .addStatement("$paramNameReader.nextNull()")
                .addStatement("return null")
                .endControlFlow()
                .addStatement("%W")
                .apply {
                    // define local variables, initialized with default values
                    propertyMethods.forEach { (p, m) ->
                        val propType = propertyType(m)
                        val nullablePropType = propType.asNullable()

                        addStatement("var $p: %T = default${p.capitalize()}", nullablePropType)
                    }

                    // read json stream
                    addStatement("%W")
                    addStatement("$paramNameReader.beginObject()")
                    beginControlFlow("while ($paramNameReader.hasNext())")
                    beginControlFlow("if ($paramNameReader.peek() == $QN_GSON_TOKEN_NULL)")
                    addStatement("$paramNameReader.nextNull()")
                    addStatement("continue")
                    endControlFlow()

                    beginControlFlow("when ($paramNameReader.nextName())")
                    propertyMethods.forEach { (p) ->
                        addStatement("%S -> $p = ${p}Adapter.read($paramNameReader)", p)
                    }
                    addStatement("else -> $paramNameReader.skipValue()")
                    endControlFlow()
                    endControlFlow()
                    addStatement("$paramNameReader.endObject()\n")

                    // null safety check
                    propertyMethods
                            .filter { (_, m) -> !propertyType(m).nullable }
                            .forEach { (p) ->
                                beginControlFlow("if ($p == null)")
                                addStatement("throw IllegalArgumentException(%S)", "$p must not be null!")
                                endControlFlow()
                            }

                    addStatement("return $concreteDataClassSimpleName(%L)",
                            propertyMethods.map { (p) -> p }.joinToString(", "))
                }
                .build()
    }

    private fun gsonWriterFunSpec(type: TypeElement, propertyMethods: Map<String, ExecutableElement>): FunSpec {
        val paramNameWriter = "jsonWriter"
        val paramNameObj = "${type.simpleName}".decapitalize()
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
                    propertyMethods.forEach { (p, _) ->
                        addStatement("$paramNameWriter.name(%S)", p)
                        addStatement("${p}Adapter.write($paramNameWriter, $paramNameObj.$p)")
                    }
                }
                .addStatement("$paramNameWriter.endObject()")
                .build()
    }

    private fun propertyType(propertyMethod: ExecutableElement): TypeName {
        val type = propertyMethod.returnType.asTypeName()
        return if (MoreElements.isAnnotationPresent(propertyMethod, Nullable::class.java)) type.asNullable() else type
    }

    private fun propertyDefaultValueDefined(propertyMethod: ExecutableElement): Boolean {
        val propDefMirror = getAnnotationMirror(propertyMethod, DataClassProp::class.java).orNull() ?: return false
        val defaultValue = getAnnotationValue(propDefMirror, DataClassProp::defaultValueLiteral.name).value as String
        return defaultValue.isNotEmpty()
    }

    companion object {
        private const val GSON_ADAPTER_CLASS_NAME = "GsonTypeAdapter"
        private const val QN_GSON_TOKEN_NULL = "com.google.gson.stream.JsonToken.NULL"
    }
}
