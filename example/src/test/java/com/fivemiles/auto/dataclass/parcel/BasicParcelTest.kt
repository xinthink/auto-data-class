package com.fivemiles.auto.dataclass.parcel

import android.os.Parcelable
import com.fivemiles.auto.dataclass.DataClass
import org.junit.Before
import org.junit.Test

@DataClass interface ParcelData : Parcelable {
    val firstName: String?
    val lastName: String?
    val greeting: String
}

/**
 * The most simple case of parcelable class
 */
class BasicParcelTest {

    @Before fun setup() = Unit

    @Test fun writeToParcel() = Unit
}
