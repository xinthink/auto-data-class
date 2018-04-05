package com.fivemiles.auto.dataclass

import com.fivemiles.auto.dataclass.GsonTypeAdapterGenerator.Companion.generatedStandAloneGsonAdapterClassName
import com.squareup.kotlinpoet.TypeSpec
import javax.annotation.processing.ProcessingEnvironment
import javax.lang.model.element.TypeElement

/**
 * Code generator for class annotated with `Parcelize`.
 *
 * For this type of class, we just need to generate a Gson TypeAdapter
 */
internal class ParcelizedClassGenerator(
    private val processingEnv: ProcessingEnvironment,
    private val errorReporter: ErrorReporter
) : Generator {
    private val facetGenerator = GsonTypeAdapterGenerator(processingEnv, errorReporter)

    override fun generate(element: TypeElement): Pair<DataClassDef, TypeSpec?> {
        val dataClassDef = DataClassDef(processingEnv, errorReporter, element)
        return dataClassDef to if (facetGenerator.isApplicable(dataClassDef))
            facetGenerator.generate(dataClassDef, generatedStandAloneGsonAdapterClassName(element))
        else null
    }
}
