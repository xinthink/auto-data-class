package com.fivemiles.auto.dataclass.parcel

import android.os.Parcel
import android.os.Parcelable
import com.fivemiles.auto.dataclass.DataClass
import org.junit.Before
import org.junit.Test

@DataClass interface ParcelData : Parcelable {
    val firstName: String?
    val lastName: String?
    val greeting: String

    override fun writeToParcel(dest: Parcel, flags: Int) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun describeContents() = 0

    companion object {
        val CREATOR = object : Parcelable.Creator<ParcelData> {
            override fun createFromParcel(source: Parcel): ParcelData {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }

            override fun newArray(size: Int): Array<ParcelData?> = arrayOfNulls(size)
        }
    }
}

/**
 * The most simple case of parcelable class
 */
class BasicParcelTest {

    @Before fun setup() {
    }

    @Test fun writeToParcel() {
    }
}
