package com.fivemiles.auto.dataclass.parcel

import android.os.Parcelable
import com.fivemiles.auto.dataclass.DataClass
import com.fivemiles.auto.dataclass.DataProp

@DataClass(generateGsonTypeAdapter = false)
interface TransientParcelData : Parcelable {
    val name: String

    /** Nullable transient prop without a default value */
    @get:DataProp(isTransient = true)
    val ntp: String?

    /** Non-nullable transient prop need a default value */
    @get:DataProp(isTransient = true, defaultValueLiteral = "\"non-nullable transient property\"")
    var tp: String
}

class TransientParcelFieldTest
