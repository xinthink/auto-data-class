package com.fivemiles.dataclass.androidexample

import android.os.Parcel
import android.os.Parcelable
import android.support.test.runner.AndroidJUnit4
import com.fivemiles.auto.dataclass.DataClass
import com.fivemiles.auto.dataclass.DataProp
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith

@DataClass(generateGsonTypeAdapter = false)
interface Address : Parcelable {
    val street: String
    val city: String

    companion object {
        fun empty(): Address = DC_Address("", "")
    }
}

@DataClass(generateGsonTypeAdapter = false)
interface Person : Parcelable {
    @get:DataProp(defaultValueLiteral = "\"\"")
    val name: String

    @get:DataProp(defaultValueLiteral = "0")
    val age: Int

    @get:DataProp(defaultValueLiteral = "Address.empty()")
    val addr: Address

    @get:DataProp(defaultValueLiteral = "null")
    val nullableAddr: Address?

    @get:DataProp(defaultValueLiteral = "emptyList()")
    val la: List<Address>

    @get:DataProp(defaultValueLiteral = "null")
    val nla: List<Address>?
}

/**
 * Instrumented test, which will execute on an Android device.
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
@RunWith(AndroidJUnit4::class)
class ParcelableTypesTest {

    @Test fun testUnmarshalling() {
        val parcel = Parcel.obtain()
        val person = DC_Person("Jane", 21,
                DC_Address("1st North Rd.", "NY"),
                DC_Address("2nd North Rd.", "NYC")
        )

        person.writeToParcel(parcel, 0)
        parcel.setDataPosition(0)

        val personRead = DC_Person.CREATOR.createFromParcel(parcel)
        assertNotNull(personRead)
        assertEquals("Jane", personRead.name)
        assertEquals(21, personRead.age)

        val addr = personRead.addr
        assertNotNull(addr)
        assertEquals("1st North Rd.", addr.street)
        assertEquals("NY", addr.city)

        val na = personRead.nullableAddr
        assertNotNull(na)
        assertEquals("2nd North Rd.", na!!.street)
        assertEquals("NYC", na.city)

        assertNotNull(personRead.la)
        assertNull(personRead.nla)
    }

    @Test fun testUnmarshallingWithNull() {
        val parcel = Parcel.obtain()
        val person = DC_Person()

        person.writeToParcel(parcel, 0)
        parcel.setDataPosition(0)

        val personRead = DC_Person.CREATOR.createFromParcel(parcel)
        assertNotNull(personRead)

        val na = personRead.nullableAddr
        assertNull(na)
    }

    @Test fun testUnmarshallingWithParcelableList() {
        val parcel = Parcel.obtain()
        val person = DC_Person(la = listOf(
                DC_Address("st1", "c1"),
                DC_Address("st2", "c2")
        ), nla = listOf(
                DC_Address("st3", "c3"),
                DC_Address("st4", "c4"),
                DC_Address("st5", "c5")
        ))

        person.writeToParcel(parcel, 0)
        parcel.setDataPosition(0)

        val personRead = DC_Person.CREATOR.createFromParcel(parcel)
        assertNotNull(personRead)

        val la = personRead.la
        assertEquals(2, la.size)
        assertEquals(DC_Address("st1", "c1"), la[0])
        assertEquals(DC_Address("st2", "c2"), la[1])

        val nla = personRead.nla
        assertNotNull(nla)
        assertEquals(3, nla!!.size)
        assertEquals(DC_Address("st3", "c3"), nla[0])
        assertEquals(DC_Address("st4", "c4"), nla[1])
        assertEquals(DC_Address("st5", "c5"), nla[2])
    }
}
