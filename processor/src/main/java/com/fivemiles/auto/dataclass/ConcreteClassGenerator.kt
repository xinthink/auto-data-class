package com.fivemiles.auto.dataclass

import com.fivemiles.auto.dataclass.GsonTypeAdapterGenerator.Companion.generatedStandAloneGsonAdapterClassName
import com.squareup.kotlinpoet.TypeSpec
import javax.annotation.processing.ProcessingEnvironment
import javax.lang.model.element.TypeElement

/**
 * Code generator for concrete class, for example, a class with `@Parcelize` annotation or a data class.
 *
 * For this type of class, just generate a stand-alone Gson TypeAdapter
 */
internal class ConcreteClassGenerator(
    private val processingEnv: ProcessingEnvironment,
    private val errorReporter: ErrorReporter
) : Generator {
    private val facetGenerator = GsonTypeAdapterGenerator()

    override fun generate(element: TypeElement): Pair<DataClassDef, TypeSpec?> {
        val dataClassDef = DataClassDef(processingEnv, errorReporter, element)
        return dataClassDef to if (facetGenerator.isApplicable(dataClassDef))
            facetGenerator.generate(dataClassDef, generatedStandAloneGsonAdapterClassName(element))
        else null
    }
}
