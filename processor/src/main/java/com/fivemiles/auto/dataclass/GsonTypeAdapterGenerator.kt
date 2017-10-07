package com.fivemiles.auto.dataclass

import com.google.auto.common.AnnotationMirrors.getAnnotationValue
import com.google.auto.common.MoreElements.getAnnotationMirror
import com.google.gson.Gson
import com.google.gson.TypeAdapter
import com.google.gson.reflect.TypeToken
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonToken
import com.google.gson.stream.JsonWriter
import com.squareup.kotlinpoet.*
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
        private val errorReporter: ErrorReporter) : FacetGenerator {

    private val typeUtils = processingEnv.typeUtils
    private val elementUtils = processingEnv.elementUtils

    private lateinit var concreteDataClassSimpleName: String

    override fun generate(dataClassDef: DataClassDef,
                          propertyMethods: Map<String, ExecutableElement>,
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
                .addProperties(propertyMethods.flatMap { (p, m) -> propDefaultAndAdapter(p, m) })
                .addFun(gsonReaderFunSpec(interfaceElement, propertyMethods))
                .addFun(gsonWriterFunSpec(interfaceElement, propertyMethods))
                .build()
        dataClassSpecBuilder.addType(adapterClsSpec)
    }

    /** define default value / typeAdapter for each property */
    private fun propDefaultAndAdapter(p: String, m: ExecutableElement): List<PropertySpec> {
        val propDefMirror = getAnnotationMirror(m, DataClassProp::class.java).orNull()
        val propType = parsePropertyType(m)

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
        val adapterType = ParameterizedTypeName.get(TypeAdapter::class.asClassName(), nonNullPropType)
        val typeAdapterProp = PropertySpec.builder("${p}Adapter", adapterType)
                .addModifiers(KModifier.PRIVATE)
                .apply {
                    // TypeAdapter delegate
                    val adapteeTypeBlock = dumpTypeToken(nonNullPropType)
                    if (nonNullPropType is ParameterizedTypeName)
                        delegate("lazy { gson.getAdapter(%L) as %T }", adapteeTypeBlock, adapterType)
                    else delegate("lazy { gson.getAdapter(%L) }", adapteeTypeBlock)
                }
                .build()

        return listOf(defaultValueProp, typeAdapterProp)
    }

    /** implements the [TypeAdapter.read] method */
    private fun gsonReaderFunSpec(type: TypeElement, propertyMethods: Map<String, ExecutableElement>): FunSpec {
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
                    propertyMethods.forEach { (p, m) ->
                        val propType = parsePropertyType(m)
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
                    propertyMethods.forEach { (p, m) -> generatePropertyReader(paramJsonReader, p, m) }
                    addStatement("else -> $paramJsonReader.skipValue()")
                    endControlFlow()
                    endControlFlow()
                    addStatement("$paramJsonReader.endObject()\n")

                    // null safety check
                    propertyMethods
                            .filter { (_, m) -> !parsePropertyType(m).nullable }
                            .forEach { (p) ->
                                beginControlFlow("if ($p == null)")
                                addStatement("throw IllegalArgumentException(%S)", "$p must not be null!")
                                endControlFlow()
                            }

                    addStatement("return $concreteDataClassSimpleName(%L)",
                            propertyMethods.keys.joinToString())
                }
                .build()
    }

    private fun FunSpec.Builder.generatePropertyReader(paramJsonReader: String,
                                                       propName: String,
                                                       propMethod: ExecutableElement) {
        // preferred json field name
        addStatement("%S -> $propName = ${propName}Adapter.read($paramJsonReader)", propName)

        // alternatives if any
        val propDefMirror = getAnnotationMirror(propMethod, DataClassProp::class.java).orNull()
        if (propDefMirror != null) {
            @Suppress("UNCHECKED_CAST")
            val alternativeNames = getAnnotationValue(propDefMirror,
                    DataClassProp::jsonFieldAlternate.name).value as List<Any>
            if (alternativeNames.size > 1) {
                addStatement("in arrayOf(%L) -> $propName = ${propName}Adapter.read($paramJsonReader)", alternativeNames)
            } else if (alternativeNames.isNotEmpty()) {
                addStatement("%L -> $propName = ${propName}Adapter.read($paramJsonReader)", alternativeNames)
            }
        }
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
