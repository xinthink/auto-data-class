package com.fivemiles.auto.dataclass.gson

import com.fivemiles.auto.dataclass.DataClass
import com.fivemiles.auto.dataclass.DataClassProp
import com.fivemiles.auto.dataclass.gson.util.TestTypeAdapterFactory
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.TypeAdapter
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

/**
 * Data class definition with properties has alternative json field name
 */
@DataClass interface AdaptiveImage {
    val src: String
        @DataClassProp(
                jsonFieldAlternate = arrayOf("link", "url")
        ) get

    companion object {
        fun typeAdapter(gson: Gson): TypeAdapter<AdaptiveImage> =
                DC_AdaptiveImage.GsonTypeAdapter(gson)
    }
}

/**
 * Created by ywu on 2017/7/18.
 */
class FieldAlternativesTest {
    lateinit var gson: Gson

    @Before fun setup() {
        gson = GsonBuilder()
                .registerTypeAdapterFactory(TestTypeAdapterFactory())
                .create()
    }

    @Test fun jsonParsingWithAlternativeFields() {
        var json = """{"url": "a.png"}"""
        var data = gson.fromJson(json, AdaptiveImage::class.java)
        assertEquals("a.png", data.src)

        json = """{"link": "a.png"}"""
        data = gson.fromJson(json, AdaptiveImage::class.java)
        assertEquals("a.png", data.src)

        json = """{"src": "a.png"}"""
        data = gson.fromJson(json, AdaptiveImage::class.java)
        assertEquals("a.png", data.src)
    }
}
