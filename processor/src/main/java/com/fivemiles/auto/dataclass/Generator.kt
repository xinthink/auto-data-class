package com.fivemiles.auto.dataclass

import com.squareup.kotlinpoet.TypeSpec
import javax.lang.model.element.TypeElement

/**
 * Data Class generator.
 */
internal interface Generator {
    fun generate(element: TypeElement): Pair<DataClassDef, TypeSpec?>
}

/**
 * Generator for a facet of the Data Class, such as Gson adapter.
 */
internal interface FacetGenerator {

    /**
     * Whether this facet is applicable to the given data class definition.
     *
     * @param dataClassDef definition context about the data class
     */
    fun isApplicable(dataClassDef: DataClassDef): Boolean = true

    /**
     * Add any facet to the given data class.
     *
     * @param dataClassDef definition context about the data class
     * @param dataClassSpecBuilder [builder instance][TypeSpec.Builder] to build the data class
     */
    fun generate(
        dataClassDef: DataClassDef,
        dataClassSpecBuilder: TypeSpec.Builder
    )
}
