package com.fivemiles.auto.dataclass

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.TypeAdapter
import com.google.gson.TypeAdapterFactory
import com.google.gson.reflect.TypeToken
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test


/**
 * Google compile-testing doesn't work for kotlin.
 * Tests annotation processor by writing testing sources.
 *
 * Created by ywu on 2017/7/16.
 */
@DataClass interface GsonData {
    val firstName: String?
    val lastName: String?
    val greeting: String

    companion object {
        fun typeAdapter(gson: Gson): TypeAdapter<GsonData> =
                DC_GsonData.GsonTypeAdapter(gson)
                        .apply {
                            defaultFirstName = "World"
                            defaultGreeting = "Hello"
                        }
    }
}

/**
 * The most simple case of data class
 */
class SimpleGsonTest {
    lateinit var gson: Gson

    @Before fun setup() {
        gson = GsonBuilder()
                .registerTypeAdapterFactory(object : TypeAdapterFactory {
                    @Suppress("UNCHECKED_CAST")
                    override fun <T : Any?> create(gson: Gson, type: TypeToken<T>?): TypeAdapter<T>? {
                        val rawType = type?.rawType ?: return null
                        return when {
                            GsonData::class.java.isAssignableFrom(rawType) -> GsonData.typeAdapter(gson)
                            else -> null
                        } as TypeAdapter<T>?
                    }
                })
                .create()
    }

    @Test fun jsonParsing() {
        val json = """{
        |  "firstName": "Jon",
        |  "lastName": "Snow",
        |  "greeting": "Hi"
        |}""".trimMargin()

        val data = gson.fromJson(json, GsonData::class.java)
        assertEquals("Jon", data.firstName)
        assertEquals("Snow", data.lastName)
        assertEquals("Hi", data.greeting)
    }

    @Test fun jsonParsingWithDefaultValues() {
        val json = """{"lastName": null}"""
        val data = gson.fromJson(json, GsonData::class.java)
        assertEquals("World", data.firstName)
        assertNull(data.lastName)
        assertEquals("Hello", data.greeting)
    }

    @Test fun jsonWriting() {
        val expectedJson = """{
        |  "firstName": "Jon",
        |  "lastName": "Snow",
        |  "greeting": "Hi"
        |}""".trimMargin().replace(Regex("\\s"), "")

        val json = gson.toJson(DC_SimpleData("Jon", "Snow", "Hi"))
        assertEquals(expectedJson, json)
    }

    @Test fun jsonWritingWithNulls() {
        val expectedJson = """{
        |  "firstName": "World",
        |  "greeting": "Hello"
        |}""".trimMargin().replace(Regex("\\s"), "")

        val json = gson.toJson(DC_SimpleData("World", null, "Hello"))
        assertEquals(expectedJson, json)
    }
}
