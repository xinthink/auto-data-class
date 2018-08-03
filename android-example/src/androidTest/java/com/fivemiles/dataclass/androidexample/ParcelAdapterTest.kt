package com.fivemiles.dataclass.androidexample

import android.os.Parcel
import android.os.Parcelable
import com.fivemiles.auto.dataclass.DataClass
import com.fivemiles.auto.dataclass.DataProp
import com.fivemiles.auto.dataclass.parcel.DateParcelAdapter
import com.fivemiles.auto.dataclass.parcel.ParcelAdapter
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import java.util.Date

/**
 * Data class definition with custom [ParcelAdapter]
 */
@DataClass
interface CustomParcelAdapterData : Parcelable {
    @get:DataProp(parcelAdapter = DateParcelAdapter::class)
    val date: Date
}

/** Test custom [ParcelAdapter] */
class ParcelAdapterTest {

    @Test fun testParcelReadWrite() {
        val now = System.currentTimeMillis()
        val parcel = Parcel.obtain()
        val data = DC_CustomParcelAdapterData(Date(now))
        data.writeToParcel(parcel, 0)

        parcel.setDataPosition(0)
        assertEquals(now, parcel.readLong())

        parcel.setDataPosition(0)
        val dataRead = DC_CustomParcelAdapterData.CREATOR.createFromParcel(parcel)
        assertNotNull(dataRead)
        assertEquals(now, dataRead.date.time)
    }
}
