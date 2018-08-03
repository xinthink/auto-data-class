package com.fivemiles.auto.dataclass.parcel

import android.os.Parcelable
import com.fivemiles.auto.dataclass.DataClass
import com.fivemiles.auto.dataclass.DataProp
import java.util.Date

/**
 * Data class definition with custom [ParcelAdapter]
 */
@DataClass
interface CustomParcelAdapterData : Parcelable {
    @get:DataProp(parcelAdapter = DateParcelAdapter::class)
    val date: Date
}

/**
 * Test custom [ParcelAdapter]
 */
class CustomParcelAdapterTest
