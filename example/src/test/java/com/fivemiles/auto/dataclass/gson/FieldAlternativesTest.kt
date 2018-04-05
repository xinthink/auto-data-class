package com.fivemiles.auto.dataclass.gson

import com.fivemiles.auto.dataclass.DataClass
import com.fivemiles.auto.dataclass.DataProp
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

/**
 * Data class definition with properties has alternative json field name
 */
@DataClass interface AdaptiveImage {
    @get:DataProp("source", jsonFieldAlternate = ["link", "url"])
    val src: String
}

/**
 * Created by ywu on 2017/7/18.
 */
class FieldAlternativesTest {
    lateinit var gson: Gson

    @Before fun setup() {
        gson = GsonBuilder()
            .registerTypeAdapterFactory(TestTypeAdapterFactory.create())
            .create()
    }

    @Test fun jsonParsingWithAlternativeFields() {
        var json = """{"url": "a.png"}"""
        var data = gson.fromJson(json, AdaptiveImage::class.java)
        assertEquals("a.png", data.src)

        json = """{"link": "a.png"}"""
        data = gson.fromJson(json, AdaptiveImage::class.java)
        assertEquals("a.png", data.src)

        json = """{"source": "a.png"}"""
        data = gson.fromJson(json, AdaptiveImage::class.java)
        assertEquals("a.png", data.src)
    }

    @Test fun jsonWritingWithCustomFieldName() {
        val img = DC_AdaptiveImage(src = "a.png")
        val json = gson.toJson(img)
        assertEquals("""{"source":"a.png"}""", json)
    }
}
