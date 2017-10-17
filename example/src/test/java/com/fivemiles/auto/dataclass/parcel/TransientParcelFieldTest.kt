package com.fivemiles.auto.dataclass.parcel

import android.os.Parcelable
import com.fivemiles.auto.dataclass.DataClass
import com.fivemiles.auto.dataclass.DataProp

@DataClass(generateGsonTypeAdapter = false)
interface TransientParcelData : Parcelable {
    val name: String

    /** Nullable transient prop without a default value */
    val ntp: String?
        @DataProp(isTransient = true) get

    /** Non-nullable transient prop need a default value */
    var tp: String
        @DataProp(isTransient = true,
                defaultValueLiteral = "\"non-nullable transient property\""
        ) get
}
