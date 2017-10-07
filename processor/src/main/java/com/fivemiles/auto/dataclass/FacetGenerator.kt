package com.fivemiles.auto.dataclass

import com.squareup.kotlinpoet.TypeSpec
import javax.lang.model.element.ExecutableElement

internal interface FacetGenerator {

    fun applicable(dataClassDef: DataClassDef): Boolean = true

    /**
     * Add any facet to the given data class.
     *
     * @param dataClassDef definition context about the data class
     * @param propertyMethods getter methods define the properties of the data class
     * @param dataClassSpecBuilder [builder instance][TypeSpec.Builder] to build the data class
     */
    fun generate(dataClassDef: DataClassDef,
                 propertyMethods: Map<String, ExecutableElement>,
                 dataClassSpecBuilder: TypeSpec.Builder)
}
