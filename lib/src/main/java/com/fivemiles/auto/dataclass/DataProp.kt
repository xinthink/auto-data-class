package com.fivemiles.auto.dataclass

import com.fivemiles.auto.dataclass.parcel.ParcelAdapter
import com.google.gson.TypeAdapter
import kotlin.reflect.KClass

/**
 * Define a Data property
 */
@Retention(AnnotationRetention.SOURCE)
@Target(AnnotationTarget.PROPERTY_GETTER)
@MustBeDocumented
annotation class DataProp(
    /** Name of the JSON field for the property */
    val jsonField: String = "",
    /** Alternative JSON field names */
    val jsonFieldAlternate: Array<String> = emptyArray(),
    /** The literal of the default value, used in generated source */
    val defaultValueLiteral: String = "",
    /** Customized Gson [TypeAdapter] for this property */
    val gsonTypeAdapter: KClass<out TypeAdapter<*>> = TypeAdapter::class,
    /** Customized [ParcelAdapter] for this property */
    val parcelAdapter: KClass<out ParcelAdapter<*>> = ParcelAdapter::class,
    /** If the property is transient */
    val isTransient: Boolean = false
)
