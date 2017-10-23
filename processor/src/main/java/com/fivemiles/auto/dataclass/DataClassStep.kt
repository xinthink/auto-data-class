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

    override fun isApplicable(element: Element): Boolean = element is TypeElement && element.kind.isInterface

    override fun processElement(element: TypeElement) {
        val adc = element.getAnnotation(annotation)
        if (adc == null) {
            // This shouldn't happen unless the compilation environment is buggy,
            // but it has happened in the past and can crash the compiler.
            errorReporter.abortWithError("annotation processor for $annotationName was invoked with a type" +
                    " that does not have that annotation; this is probably a compiler bug", element)
        }
        if (!isApplicable(element)) {
            errorReporter.abortWithError("$annotationName only applies to interfaces", element)
        }

        abortIfNested(element)

        val (dataClassDef, dataClassSpec) = dataClassGenerator.generate(element)
        generateFile(element, dataClassSpec)
        _processedDataClasses.add(dataClassDef)
    }
}
