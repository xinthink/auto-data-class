package com.fivemiles.auto.dataclass.parcel

import android.os.Parcelable
import com.fivemiles.auto.dataclass.DataClass
import com.fivemiles.auto.dataclass.DataProp
import java.util.*

/**
 * Data class definition with custom Gson [ParcelAdapter]
 */
@DataClass
interface CustomParcelAdapterData : Parcelable {
    val date: Date
        @DataProp(
                parcelAdapter = DateParcelAdapter::class
        ) get
}

/**
 * Test custom [ParcelAdapter]
 */
class CustomParcelAdapterTest
