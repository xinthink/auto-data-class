/*
 * Example for overriding built-in methods
 */
package com.fivemiles.auto.dataclass

import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * The data class definition, internal usage only.
 */
@DataClass internal interface PersonInternal {
    val name: String
    val age: Int
}

/**
 * The public wrapper of the data class, in which you can rewrite the built-in methods.
 */
class Person
private constructor(p: PersonInternal) : PersonInternal by p {

    override fun toString(): String {
        return "My name is $name, I'm $age years old."
    }

    companion object {
        internal fun create(p: PersonInternal): Person = Person(p)
        fun create(name: String, age: Int): Person = Person(DC_PersonInternal(name, age))
    }
}


/**
 * Example for overriding built-in methods
 */
class OverridingTest {

    @Test fun testToString() {
        val person = Person.create("Jon", 24)
        assertEquals("Jon", person.name)
        assertEquals(24, person.age)
        assertEquals("My name is Jon, I'm 24 years old.", person.toString())
    }
}
