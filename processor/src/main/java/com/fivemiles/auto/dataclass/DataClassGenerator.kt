package com.fivemiles.auto.dataclass

import com.google.auto.common.AnnotationMirrors.getAnnotationValue
import com.squareup.kotlinpoet.*
import javax.annotation.Generated
import javax.annotation.processing.ProcessingEnvironment
import javax.lang.model.element.TypeElement


/**
 * Generates data class given a TypeElement
 *
 * Created by ywu on 2017/7/10.
 */
internal class DataClassGenerator(
        private val processingEnv: ProcessingEnvironment,
        private val errorReporter: ErrorReporter) {

    private val facetGenerators: List<FacetGenerator> = listOf(
            GsonTypeAdapterGenerator(processingEnv, errorReporter),
            ParcelableGenerator(processingEnv, errorReporter)
    )

    /**
     * Build a data class TypeSpec for the given TypeElement
     *
     * @param element the interface element which defines the data class
     */
    @Suppress("UNCHECKED_CAST")
    fun generate(element: TypeElement): TypeSpec {
        val dataClassDef = DataClassDef(processingEnv, errorReporter, element)

        val builder = TypeSpec.classBuilder(dataClassDef.className)
                .addSuperinterface(element.asClassName())
                .addModifiers(KModifier.DATA, KModifier.INTERNAL)
                .addAnnotation(AnnotationSpec.builder(Generated::class)
                        .addMember("value", "\"${DataClassAnnotationProcessor::class.qualifiedName}\"")
                        .build())
                .addAnnotation(AnnotationSpec.builder(Suppress::class)
//                        .addMember("names", "*arrayOf(%S)", "UNCHECKED_CAST")
                        .addMember("value", "\"UNCHECKED_CAST\"")
                        .build())
                .generateProperties(dataClassDef.properties)

        // add extra facets to the data class
        facetGenerators.forEach {
            if (it.applicable(dataClassDef)) {
                it.generate(dataClassDef, builder)
            }
        }
        return builder.build()
    }

    // add stuff with properties
    private fun TypeSpec.Builder.generateProperties(properties: Set<DataPropDef>): TypeSpec.Builder {
        val constructorBuilder = FunSpec.constructorBuilder()
        properties.forEach {
            // default value
            val ctorParamBuilder = ParameterSpec.builder(it.name, it.typeKt)
            val propDefMirror = it.dataPropAnnotation
            if (propDefMirror != null) {
                val defaultValue = getAnnotationValue(propDefMirror,
                        DataProp::defaultValueLiteral.name).value as String
                if (defaultValue.isNotBlank()) {
                    ctorParamBuilder.defaultValue("%L", defaultValue)
                }
            }

            constructorBuilder.addParameter(ctorParamBuilder.build())
            addProperty(PropertySpec.builder(it.name, it.typeKt)
                    .mutable(it.isMutable)
                    .addModifiers(KModifier.OVERRIDE)
                    .initializer(it.name)
                    .build())
        }

        return primaryConstructor(constructorBuilder.build())
    }
}
