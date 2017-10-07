package com.fivemiles.auto.dataclass.gson

import com.fivemiles.auto.dataclass.DataClass
import com.fivemiles.auto.dataclass.gson.util.TestTypeAdapterFactory
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.TypeAdapter
import com.google.gson.reflect.TypeToken
import com.squareup.kotlinpoet.asTypeName
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@DataClass interface FledgedGsonData {
    val s: String
    val i: Int
    val sh: Short
    val b: Byte
    val f: Float
    val d: Double
    val z: Boolean
    val ns: String?
    val ls: List<String>
//    val lns: List<String?>  // nullable type param unsupported
    val msi: Map<String, Int>
    val ss: Set<String>
    val lmsi: List<Map<String, Int>>

    companion object {
        fun typeAdapter(gson: Gson): TypeAdapter<FledgedGsonData> =
                DC_FledgedGsonData.GsonTypeAdapter(gson)
    }
}

/**
 * Demonstrating supported property types.
 */
class GsonFieldTypesTest {
    lateinit var gson: Gson

    @Before fun setup() {
        gson = GsonBuilder()
                .registerTypeAdapterFactory(TestTypeAdapterFactory())
                .create()
    }

    @Test fun readParameterizedProps() {
        val json = """{
        |  "s": "Abc",
        |  "i": 10,
        |  "sh": 512,
        |  "b": 255,
        |  "f": 3.5,
        |  "d": 3.5,
        |  "z": true,
        |  "ls": ["Ten", "11", "All"],
        |  "msi": {
        |    "Two": 2,
        |    "Ten": 10
        |  },
        |  "ss": ["12", "Zero"],
        |  "lmsi": [{
        |    "11": 11,
        |    "12": 12
        |  }, {
        |    "21": 21,
        |    "22": 22,
        |    "23": 23
        |  }]
        |}""".trimMargin()

        val data = gson.fromJson(json, FledgedGsonData::class.java)
        assertEquals(512.toShort(), data.sh)
        assertEquals(255.toByte(), data.b)

        // parameterized types
        assertEquals(listOf("Ten", "11", "All"), data.ls)
        assertEquals(setOf("12", "Zero"), data.ss)
        assertEquals(mapOf("Two" to 2, "Ten" to 10), data.msi)
        assertEquals(listOf(
                mapOf("11" to 11,
                        "12" to 12),
                mapOf("21" to 21,
                        "22" to 22,
                        "23" to 23)
        ), data.lmsi)
    }
}
