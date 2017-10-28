package com.fivemiles.auto.dataclass

import com.squareup.kotlinpoet.TypeSpec

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
    fun generate(dataClassDef: DataClassDef,
                 dataClassSpecBuilder: TypeSpec.Builder)
}
