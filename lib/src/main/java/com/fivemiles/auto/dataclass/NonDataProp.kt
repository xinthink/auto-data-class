package com.fivemiles.auto.dataclass

/**
 * Tell the generators to ignore the annotated getters or methods.
 */
@Retention(AnnotationRetention.SOURCE)
@Target(AnnotationTarget.PROPERTY_GETTER, AnnotationTarget.FUNCTION)
@MustBeDocumented
annotation class NonDataProp
