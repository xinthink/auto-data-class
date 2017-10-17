package com.fivemiles.auto.dataclass

import com.squareup.kotlinpoet.TypeSpec
import javax.lang.model.element.ExecutableElement

internal interface FacetGenerator {

    /**
     * Whether this facet is applicable to the given data class definition.
     *
     * @param dataClassDef definition context about the data class
     */
    fun applicable(dataClassDef: DataClassDef): Boolean = true

    /**
     * Add any facet to the given data class.
     *
     * @param dataClassDef definition context about the data class
     * @param dataClassSpecBuilder [builder instance][TypeSpec.Builder] to build the data class
     */
    fun generate(dataClassDef: DataClassDef,
                 dataClassSpecBuilder: TypeSpec.Builder)
}
