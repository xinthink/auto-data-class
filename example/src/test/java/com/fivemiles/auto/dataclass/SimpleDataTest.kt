/*
 * Google compile-testing doesn't work for kotlin.
 * Tests annotation processor by writing testing sources.
 *
 * Created by ywu on 2017/7/16.
 */
package com.fivemiles.auto.dataclass

import android.os.Parcelable
import com.google.gson.Gson
import com.google.gson.TypeAdapter
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

/**
 * Data class definition using interfaces.
 */
@DataClass interface SimpleData : Parcelable {
    val firstName: String?

    @get:DataProp(defaultValueLiteral = "null")
    val lastName: String?

    @get:DataProp(defaultValueLiteral = "\"Hello\"")
    val greeting: String

    @get:DataProp(defaultValueLiteral = "0L")
    var mutableProp: Long

    // derived property
    val fullName: String
        get() = "${firstName ?: ""} ${lastName ?: ""}".trim()

    // function with default implementation (Actually you can't declare abstract functions here)
    fun sayHello(): CharSequence = if (firstName.isNullOrBlank()) greeting else "$greeting, $firstName"
}

/**
 * Or abstract classes is also supported.
 */
@Suppress("unused")
@DataClass abstract class SimpleAbsData : Parcelable {
    abstract val name: String
    abstract var age: Int

    /** Getter-like concrete methods will be skipped */
    fun description(): String = "$name is $age years old."

    companion object {
        fun create(name: String = "Jon", age: Int = 18): SimpleAbsData =
            DC_SimpleAbsData(name, age)

        fun typeAdapter(gson: Gson): TypeAdapter<SimpleAbsData> =
            DC_SimpleAbsData.GsonTypeAdapter(gson)
    }
}

/**
 * Use data class directly if Parcelable if not required
 */
@DataClass data class SimpleFinalData(val name: String, var age: Int) {
    override fun toString() = "$name, $age years old"

    /** Skip getter-like methods by annotating [NonDataProp] */
    @NonDataProp
    fun description(): String = "$name is $age years old."
}

/**
 * The most simple case of data class
 */
class SimpleDataTest {

    /**
     * Test instantiation of data classes defined by interfaces
     */
    @Test fun instantiate() {
        val data = DC_SimpleData("Jon", "Snow", "Hi", 0L)
        assertEquals("Jon", data.firstName)
        assertEquals("Snow", data.lastName)
        assertEquals("Jon Snow", data.fullName)
        assertEquals("Hi", data.greeting)
        assertEquals("Hi, Jon", data.sayHello())
    }

    @Test fun defaultProperties() {
        val data = DC_SimpleData("World")
        assertEquals("World", data.firstName)
        assertNull(data.lastName)
        assertEquals("World", data.fullName)
        assertEquals("Hello", data.greeting)
        assertEquals("Hello, World", data.sayHello())
        assertEquals(0L, data.mutableProp)
    }

    @Test fun allowMutableProperties() {
        val data = DC_SimpleData("World", mutableProp = 1L)
        assertEquals(1L, data.mutableProp)

        data.mutableProp = 1024L
        assertEquals(1024L, data.mutableProp)
    }

    /**
     * Test instantiation of data classes defined by abstract classes
     */
    @Test fun instantiateAbsClz() {
        val data = SimpleAbsData.create()
        assertEquals("Jon", data.name)
        assertEquals(18, data.age)
    }
}
