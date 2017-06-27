package io.github.xinthink.auto.dataclass

/**
 * Created by ywu on 2017/6/26.
 */

/**
 * Exception thrown when annotation processing should be aborted for the current class. Processing
 * can continue on other classes. Throwing this exception does not cause a compiler error, so either
 * one should explicitly be emitted or it should be clear that the compiler will be producing its
 * own error for other reasons.
 */
internal class AbortProcessingException : RuntimeException()

/**
 * Exception thrown in the specific case where processing of a class was abandoned because it
 * required types that the class references to be present and they were not. This case is handled
 * specially because it is possible that those types might be generated later during annotation
 * processing, so we should reattempt the processing of the class in a later annotation processing
 * round.
 */
internal class MissingTypeException : RuntimeException()
