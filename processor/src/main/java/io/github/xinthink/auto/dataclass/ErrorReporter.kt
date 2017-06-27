package io.github.xinthink.auto.dataclass

import javax.annotation.processing.ProcessingEnvironment
import javax.lang.model.element.Element
import javax.tools.Diagnostic

/**
 * Handle error reporting for an annotation processor.
 *
 * Created by ywu on 2017/6/26.
 */
internal class ErrorReporter(processingEnv: ProcessingEnvironment) {
    private val messager = processingEnv.messager

    /**
     * Issue a compilation note.
     * @param msg the text of the note
     * @param e the element to which it pertains
     */
    fun reportNote(msg: String, e: Element) = messager.printMessage(Diagnostic.Kind.NOTE, msg, e)

    /**
     * Issue a compilation warning.
     * @param msg the text of the warning
     * @param e the element to which it pertains
     */
    fun reportWarning(msg: String, e: Element) = messager.printMessage(Diagnostic.Kind.WARNING, msg, e)

    /**
     * Issue a compilation error. This method does not throw an exception, since we want to continue
     * processing and perhaps report other errors. It is a good idea to introduce a test case in
     * CompilationTest for any new call to reportError(...) to ensure that we continue correctly after
     * an error.
     * @param msg the text of the warning
     * @param e the element to which it pertains
     */
    fun reportError(msg: String, e: Element) = messager.printMessage(Diagnostic.Kind.ERROR, msg, e)

    /**
     * Issue a compilation error and abandon the processing of this class. This does not prevent the
     * processing of other classes.
     * @param msg the text of the error
     * @param e the element to which it pertains
     */
    fun abortWithError(msg: String, e: Element) {
        reportError(msg, e)
        throw AbortProcessingException()
    }
}
