package com.fivemiles.auto.dataclass

import javax.annotation.processing.ProcessingEnvironment
import javax.lang.model.element.Element
import javax.lang.model.element.TypeElement

/**
 * Concrete logic processing the Data Classes
 */
internal class DataClassStep(processingEnv: ProcessingEnvironment,
                             sourceLocationManager: SourceLocationManager
) : AbstractProcessingStep(processingEnv, sourceLocationManager) {
    private val dataClassGenerator = DataClassGenerator(processingEnv, errorReporter)

    private val _processedDataClasses: MutableSet<DataClassDef> = mutableSetOf()

    /**
     * Retrieve all the processed Data Class definitions
     */
    val processedDataClasses: Set<DataClassDef> get() = _processedDataClasses

    override val annotation = DataClass::class.java

    override fun isApplicable(element: Element): Boolean = element.isInterfaceOrAbstractClass

    override fun doProcessElement(element: TypeElement) {
        val (dataClassDef, dataClassSpec) = dataClassGenerator.generate(element)
        generateFile(element, dataClassSpec)
        _processedDataClasses.add(dataClassDef)
    }
}
