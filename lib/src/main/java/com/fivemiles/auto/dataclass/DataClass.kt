package com.fivemiles.auto.dataclass

import com.google.gson.TypeAdapter

/**
 * Annotation to define a Data Class
 */
@Retention(AnnotationRetention.SOURCE)
@Target(AnnotationTarget.CLASS)
@MustBeDocumented
annotation class DataClass(
        /** Whether a Gson [TypeAdapter] should be generated for this Data Class, the default is true */
        val generateGsonTypeAdapter: Boolean = true
)
