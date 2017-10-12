package com.fivemiles.auto.dataclass

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

/**
 * Google compile-testing doesn't work for kotlin.
 * Tests annotation processor by writing testing sources.
 *
 * Created by ywu on 2017/7/16.
 */
@DataClass interface SimpleData {
    val firstName: String?
    val lastName: String?
        @DataProp(defaultValueLiteral = "null") get

    val greeting: String
        @DataProp(defaultValueLiteral = "\"Hello\"") get

    // derived property
    val fullName: String
        get() = "${firstName ?: ""} ${lastName ?: ""}".trim()

    // function with default implementation (Actually you can't declare abstract functions here)
    fun sayHello(): CharSequence = if (firstName.isNullOrBlank()) greeting else "$greeting, $firstName"
}

/**
 * The most simple case of data class
 */
class SimpleDataTest {

    @Test fun instantiate() {
        val data = DC_SimpleData("Jon", "Snow", "Hi")
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
    }
}
