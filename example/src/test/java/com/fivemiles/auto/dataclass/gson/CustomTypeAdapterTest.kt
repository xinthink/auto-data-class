package com.fivemiles.auto.dataclass.gson

import com.fivemiles.auto.dataclass.DataClass
import com.fivemiles.auto.dataclass.DataClassProp
import com.fivemiles.auto.dataclass.gson.util.TestTypeAdapterFactory
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.TypeAdapter
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Data class definition with custom Gson [TypeAdapter]
 */
@DataClass interface CustomAdapterData {
    val numeralBool: Boolean
        @DataClassProp(
                gsonTypeAdapter = NumeralBooleanAdapter::class
        ) get
    val jsonBool: Boolean

    companion object {
        fun typeAdapter(gson: Gson): TypeAdapter<CustomAdapterData> =
                DC_CustomAdapterData.GsonTypeAdapter(gson)
    }
}

/**
 * Test custom Gson [TypeAdapter]
 */
class CustomTypeAdapterTest {
    lateinit var gson: Gson

    @Before fun setup() {
        gson = GsonBuilder()
                .registerTypeAdapterFactory(TestTypeAdapterFactory())
                .create()
    }

    /**
     * 1: true, false otherwise
     */
    @Test fun parseNumberAsBoolean() {
        var json = """{"numeralBool": 1, "jsonBool": true}"""
        var data = gson.fromJson(json, CustomAdapterData::class.java)
        assertTrue(data.numeralBool)
        assertTrue(data.jsonBool)

        json = """{"numeralBool": 0, "jsonBool": false}"""
        data = gson.fromJson(json, CustomAdapterData::class.java)
        assertFalse(data.numeralBool)
        assertFalse(data.jsonBool)

        json = """{"numeralBool": 2, "jsonBool": false}"""
        data = gson.fromJson(json, CustomAdapterData::class.java)
        assertFalse(data.numeralBool)
        assertFalse(data.jsonBool)
    }

    /**
     * Parsing boolean from compatible types
     */
    @Test fun parseCompatibleBoolean() {
        var json = """{"numeralBool": true, "jsonBool": true}"""
        var data = gson.fromJson(json, CustomAdapterData::class.java)
        assertTrue(data.numeralBool)

        json = """{"numeralBool": "TrUe", "jsonBool": false}"""
        data = gson.fromJson(json, CustomAdapterData::class.java)
        assertTrue(data.numeralBool)
        
        json = """{"numeralBool": "false", "jsonBool": false}"""
        data = gson.fromJson(json, CustomAdapterData::class.java)
        assertFalse(data.numeralBool)

        json = """{"numeralBool": "truetrue", "jsonBool": false}"""
        data = gson.fromJson(json, CustomAdapterData::class.java)
        assertFalse(data.numeralBool)
    }
}
