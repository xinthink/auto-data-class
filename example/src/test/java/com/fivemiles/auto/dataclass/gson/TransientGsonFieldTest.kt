package com.fivemiles.auto.dataclass.gson


import com.fivemiles.auto.dataclass.DataClass
import com.fivemiles.auto.dataclass.DataProp
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

@DataClass interface TransientGsonData {
    val name: String

    /** Nullable transient prop without a default value */
    @get:DataProp(isTransient = true)
    val ntp: String?

    /** Non-nullable transient prop need a default value */
    @get:DataProp(isTransient = true,
            defaultValueLiteral = "\"non-nullable transient property\""
    )
    var tp: String
}

class TransientGsonFieldTest {
    lateinit var gson: Gson

    @Before fun setup() {
        gson = GsonBuilder()
                .registerTypeAdapterFactory(TestTypeAdapterFactory.create())
                .create()
    }

    @Test fun ignoreTransientFieldsWhenParsingJson() {
        val json = """{
        |  "name": "Abc",
        |  "ntp": "transient field should be ignored",
        |  "tp": "transient field should be ignored"
        |}""".trimMargin()

        val data = gson.fromJson(json, TransientGsonData::class.java)
        assertEquals("Abc", data.name)
        assertNull(data.ntp)
        assertEquals("non-nullable transient property", data.tp)
    }

    @Test fun ignoreTransientFieldsWhenWritingJson() {
        val data: TransientGsonData = DC_TransientGsonData("Jon")
        assertEquals("non-nullable transient property", data.tp)

        val json = gson.toJson(data)
        assertEquals("""{"name":"Jon"}""", json)
    }
}
