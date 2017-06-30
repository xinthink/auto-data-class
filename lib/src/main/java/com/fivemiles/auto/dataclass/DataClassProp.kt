package com.fivemiles.auto.dataclass

/**
 * Define a Data property
 */
@Retention(AnnotationRetention.SOURCE)
@Target(AnnotationTarget.PROPERTY_GETTER)
@MustBeDocumented
annotation class DataClassProp(
        /** Name of the JSON field for the property */
        val jsonField: String = "",
        /** Alternative JSON field names */
        val jsonFieldAlternate: Array<String> = emptyArray(),
        /** The literal of the default value, used in generated source */
        val defaultValueLiteral: String = ""
//      val gsonTypeAdapter:
//      val parcelTypeAdapter:
)
