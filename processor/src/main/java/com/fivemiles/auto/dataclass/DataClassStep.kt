package com.fivemiles.auto.dataclass

import javax.annotation.processing.ProcessingEnvironment
import javax.lang.model.element.Element
import javax.lang.model.element.TypeElement

/**
 * Concrete logic processing the Data Classes
 */
internal class DataClassStep(
    private val processingEnv: ProcessingEnvironment,
    sourceLocationManager: SourceLocationManager
) : AbstractProcessingStep(processingEnv, sourceLocationManager) {

    private val _processedDataClasses: MutableSet<DataClassDef> = mutableSetOf()

    /**
     * Retrieve all the processed Data Class definitions
     */
    val processedDataClasses: Set<DataClassDef> get() = _processedDataClasses

    override val annotation = DataClass::class.java

    override fun isApplicable(element: Element): Boolean =
        element.isInterfaceOrAbstractClass || element.isConcreteClass

    override fun doProcessElement(element: TypeElement) {
        val generator = createGenerator(element)
        val (dataClassDef, dataClassSpec) = generator.generate(element)
        if (dataClassSpec != null) {
            generateFile(element, dataClassSpec)
            _processedDataClasses.add(dataClassDef)
        }
    }

    private fun createGenerator(element: TypeElement): Generator {
        val ctr = if (element.isConcreteClass) ::ConcreteClassGenerator else ::DataClassGenerator
        return ctr(processingEnv, errorReporter)
    }
}
