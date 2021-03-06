package com.fivemiles.auto.dataclass

/* ktlint-disable no-wildcard-imports */
import com.google.auto.common.GeneratedAnnotations.generatedAnnotation
import com.squareup.kotlinpoet.*
import javax.annotation.processing.ProcessingEnvironment
import javax.lang.model.SourceVersion
import javax.lang.model.element.TypeElement

/**
 * Generates data class given a TypeElement
 *
 * Created by ywu on 2017/7/10.
 */
internal class DataClassGenerator(
    private val processingEnv: ProcessingEnvironment,
    private val errorReporter: ErrorReporter
) : Generator {

    private val elementUtils = processingEnv.elementUtils

    private val facetGenerators: List<FacetGenerator> = listOf(
        GsonTypeAdapterGenerator(),
        ParcelableGenerator(processingEnv)
    )

    /**
     * Build a data class TypeSpec for the given TypeElement
     *
     * @param element the interface element which defines the data class
     */
    @Suppress("UNCHECKED_CAST")
    override fun generate(element: TypeElement): Pair<DataClassDef, TypeSpec?> {
        val dataClassDef = DataClassDef(processingEnv, errorReporter, element)

        val builder = TypeSpec.classBuilder(dataClassDef.className)
            .apply {
                if (element.kind.isInterface) addSuperinterface(element.asClassName())
                else superclass(element.asClassName())
            }
            .addModifiers(KModifier.DATA, KModifier.INTERNAL)
            .addAnnotation(AnnotationSpec.builder(Suppress::class)
//                        .addMember("names", "*arrayOf(%S)", "UNCHECKED_CAST")
                .addMember("value", "\"UNCHECKED_CAST\"")
                .build())
            .generatedAnnotation()
            .generateProperties(dataClassDef.properties)

        // add extra facets to the data class
        facetGenerators.forEach {
            if (it.isApplicable(dataClassDef)) {
                it.generate(dataClassDef, builder)
            }
        }
        return dataClassDef to builder.build()
    }

    // add @Generated annotation
    private fun TypeSpec.Builder.generatedAnnotation(): TypeSpec.Builder =
        generatedAnnotation(elementUtils, SourceVersion.latestSupported())
            .map {
                val generated = AnnotationSpec.builder(it.asClassName())
                    .addMember("value", "\"${DataClassAnnotationProcessor::class.qualifiedName}\"")
                    .build()
                addAnnotation(generated)
            }
            .orElse(this)

    // add stuff with properties
    private fun TypeSpec.Builder.generateProperties(properties: Set<DataPropDef>): TypeSpec.Builder {
        val constructorBuilder = FunSpec.constructorBuilder()
        properties.forEach {
            val ctorParamBuilder = ParameterSpec.builder(it.name, it.typeKt)
            val isTransient = it.isTransient
            val defaultValue = it.defaultValueLiteral

            // add to constructor if non-transient
            if (!isTransient) {
                if (defaultValue.isNotBlank()) {
                    ctorParamBuilder.defaultValue("%L", defaultValue)
                }
                constructorBuilder.addParameter(ctorParamBuilder.build())
            }

            // property declaration
            addProperty(PropertySpec.builder(it.name, it.typeKt)
                .mutable(it.isMutable)
                .addModifiers(KModifier.OVERRIDE)
                .apply {
                    if (!isTransient) {
                        initializer(it.name)
                        return@apply
                    }

                    // transient property should has a default value if non-nullable
                    when {
                        defaultValue.isNotBlank() -> initializer(defaultValue)
                        it.typeKt.nullable -> initializer("null")
                        else -> errorReporter.reportError(
                            "default value should be provided for a non-nullable transient property",
                            it.element)
                    }

                    addAnnotation(Transient::class)
                }
                .build())
        }

        return primaryConstructor(constructorBuilder.build())
    }
}
