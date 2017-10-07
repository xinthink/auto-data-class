package com.fivemiles.auto.dataclass.parcel

import android.os.Parcel
import android.os.Parcelable
import com.fivemiles.auto.dataclass.DataClass
import org.junit.Before
import org.junit.Test

@DataClass interface AllParcelableTypes : Parcelable {
    val i: Int
    val b: Byte
    val l: Long
    val sh: Short
    val bool: Boolean
    val c: Char
    val f: Float
    val d: Double
    val s: String
    val ns: String?
//    val ls: List<String>
//    val nls: List<String?>  // nullable type param unsupported
//    val ai: Array<Int>
//    val nai: Array<Int?>

    override fun describeContents() = 0
    override fun writeToParcel(dest: Parcel?, flags: Int) = Unit
}

/**
 * The most simple case of parcelable class
 */
class ParcelableTypesTest {

    @Before fun setup() {
    }

    @Test fun writeToParcel() {
    }
}
