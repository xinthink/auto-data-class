package com.fivemiles.auto.dataclass

import com.fivemiles.auto.dataclass.GsonTypeAdapterGenerator.Companion.GSON_ADAPTER_CLASS_NAME
import com.fivemiles.auto.dataclass.gson.GsonTypeAdapterFactory
import com.google.gson.Gson
import com.google.gson.TypeAdapter
import com.google.gson.TypeAdapterFactory
import com.google.gson.reflect.TypeToken
import com.squareup.kotlinpoet.*
import javax.annotation.processing.ProcessingEnvironment
import javax.lang.model.element.Element
import javax.lang.model.element.ExecutableElement
import javax.lang.model.element.Modifier
import javax.lang.model.element.TypeElement

/**
 * Processing @[GsonTypeAdapterFactory], to generate a registry for all Gson [com.google.gson.TypeAdapter]s
 */
internal class GsonTypeAdapterFactoryStep(processingEnv: ProcessingEnvironment,
                                          sourceLocationManager: SourceLocationManager,
                                          private val allDataClasses: Set<DataClassDef>
) : AbstractProcessingStep(processingEnv, sourceLocationManager) {

    private val typeUtils = processingEnv.typeUtils
    private val elementUtils = processingEnv.elementUtils

    private val gsonParamName: String = "gson"
    private val typeTokenParamName: String = "type"

    override val annotation = GsonTypeAdapterFactory::class.java

    override fun isApplicable(element: Element): Boolean =
            element.kind.isInterface || element.kind.isClass

    override fun doProcessElement(element: TypeElement) {
        val spec = generateTypeAdapterFactory(element)
        generateFile(element, spec)
    }

    private fun generateTypeAdapterFactory(element: TypeElement): TypeSpec {
        val factoryClsName = generatedClassName(element)
        val isAdapterFactory = isTypeAdapterFactory(element)
        val superType = if (isAdapterFactory) element.asClassName() else TypeAdapterFactory::class.asClassName()

        return TypeSpec.classBuilder(factoryClsName)
                .apply {
                    if (!isAdapterFactory || element.kind.isInterface) addSuperinterface(superType)
                    else superclass(superType)

                    // if base type is the annotated element, hide the generated implement
                    if (isAdapterFactory) KModifier.INTERNAL
                }
                .addAdapterCreationFun()
                .build()
    }

    // check given type is a TypeAdapterFactory
    private fun isTypeAdapterFactory(element: TypeElement): Boolean {
        val factoryType = elementUtils.getTypeElement(TypeAdapterFactory::class.qualifiedName)?.asType()
        return factoryType != null && typeUtils.isAssignable(element.asType(), factoryType)
    }

    /** implements the [TypeAdapterFactory.create] function */
    private fun TypeSpec.Builder.addAdapterCreationFun(): TypeSpec.Builder {
        val typeParam = TypeVariableName("T", Any::class.asTypeName().asNullable())
        val returnType = ParameterizedTypeName.get(TypeAdapter::class.asTypeName(), typeParam).asNullable()
        val tokenParamType = ParameterizedTypeName.get(TypeToken::class.asTypeName(), typeParam).asNullable()

        return addFunction(FunSpec.builder("create")
                .addTypeVariable(typeParam)
                .addModifiers(KModifier.OVERRIDE)
                .addParameter(gsonParamName, Gson::class)
                .addParameter(typeTokenParamName, tokenParamType)
                .returns(returnType)
                .addAnnotation(AnnotationSpec.builder(Suppress::class)
                        .addMember("value", "\"UNCHECKED_CAST\"")
                        .build())
                .addCode(CodeBlock.builder()
                        .addAdapterCreationBlock()
                        .add(" as %T", returnType)
                        .build())
                .build())
    }

    private fun CodeBlock.Builder.addAdapterCreationBlock(): CodeBlock.Builder {
        val rawTypeValName = "rawType"
        addStatement("val %L = %L?.rawType ?: return null", rawTypeValName, typeTokenParamName)
        add("return ").beginControlFlow("when")

        allDataClasses.forEach {
            if (!it.isGenerateGsonTypeAdapter) return@forEach

            val dcIntf = it.element.asClassName()  // interface defines the data class
            val dcImpl = it.className  // class implements the data class
            val factoryMethod = findFactoryMethod(it.element)?.simpleName
            addStatement("%T::class.java.isAssignableFrom(%L) -> %T.%L(%L)",
                    dcIntf, rawTypeValName,
                    if (factoryMethod != null) dcIntf else dcImpl,
                    factoryMethod ?: GSON_ADAPTER_CLASS_NAME,
                    gsonParamName)
        }
        addStatement("else -> null")

        return endControlFlow()
    }

    private fun findCompanionObject(element: TypeElement): TypeElement? =
            element.enclosedElements.find {
                it is TypeElement && "${it.simpleName}" == "Companion"
            } as TypeElement?

    private fun findFactoryMethod(element: TypeElement): ExecutableElement? {
        val typeAdapterType = ParameterizedTypeName.get(TypeAdapter::class.asClassName(), element.asClassName())
        return findCompanionObject(element)?.enclosedElements?.find {
            it is ExecutableElement &&
                    Modifier.ABSTRACT !in it.modifiers &&
                    it.returnType.asTypeName() == typeAdapterType
        } as ExecutableElement?
    }
}
