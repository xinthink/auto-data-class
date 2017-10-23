package com.fivemiles.auto.dataclass.gson

/**
 * Annotate class or interface indicating a [com.google.gson.TypeAdapterFactory]
 * should be generated, which can be used to instantiate the generated [com.google.gson.TypeAdapter]
 */
@Retention(AnnotationRetention.SOURCE)
@Target(AnnotationTarget.CLASS)
@MustBeDocumented
annotation class GsonTypeAdapterFactory
