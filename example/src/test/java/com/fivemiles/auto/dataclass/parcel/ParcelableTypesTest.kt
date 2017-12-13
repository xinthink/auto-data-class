package com.fivemiles.auto.dataclass.parcel

import android.os.Parcelable
import com.fivemiles.auto.dataclass.DataClass
import org.junit.Before
import org.junit.Test

@DataClass(generateGsonTypeAdapter = false)
interface ParcelableType : Parcelable {
    val s: String
}

@DataClass(generateGsonTypeAdapter = false)
interface AllParcelableTypes : Parcelable {
    val i: Int
    val ni: Int?
    val b: Byte
    val nb: Byte?
    val l: Long
    val nl: Long?
    val sh: Short
    val nsh: Short?
    val bool: Boolean
    val nbool: Boolean?
    val c: Char
    val nc: Char?
    val f: Float
    val nf: Float?
    val d: Double
    val nd: Double?
    val s: String
    val ns: String?
    val ls: List<String>
    val nls: List<String>?
//    val lns: List<String?>  // nullable type param not supported
//    val ai: Array<Int>  // `Array` is not supported
    val ss: Set<String>
    val nss: Set<String>?
    val rm: Map<String, Any>
    val nrm: Map<String, Any>?
    val lmss: List<Map<String, String>>
    val mrm: Map<String, Map<String, Any>>
    val p: ParcelableType?
}

/**
 * Demonstrating supported property types.
 */
class ParcelableTypesTest {

    @Before fun setup() = Unit

    @Test fun writeToParcel() = Unit
}
