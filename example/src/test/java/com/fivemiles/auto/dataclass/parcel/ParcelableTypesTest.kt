package com.fivemiles.auto.dataclass.parcel

import android.os.Parcelable
import com.fivemiles.auto.dataclass.DataClass
import org.junit.Before
import org.junit.Test

@DataClass(generateGsonTypeAdapter = false)
interface AllParcelableTypes : Parcelable {
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
    val ls: List<String>
//    val lns: List<String?>  // nullable type param not supported
//    val ai: Array<Int>  // `Array` is not supported
    val ss: Set<String>
    val rm: Map<String, Any>
    val lmss: List<Map<String, String>>
    val mrm: Map<String, Map<String, Any>>
}

/**
 * Demonstrating supported property types.
 */
class ParcelableTypesTest {

    @Before fun setup() = Unit

    @Test fun writeToParcel() = Unit
}
