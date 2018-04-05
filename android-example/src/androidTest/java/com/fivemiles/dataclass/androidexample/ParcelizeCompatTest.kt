package com.fivemiles.dataclass.androidexample

/* ktlint-disable no-wildcard-imports */
import android.os.Parcel
import android.os.Parcelable
import android.support.test.runner.AndroidJUnit4
import com.fivemiles.auto.dataclass.DataClass
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import kotlinx.android.parcel.Parcelize
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * When you use [Parcelize], the [Parcelable] stuff is handled by the Kotlin compiler.
 * The only thing the [auto-data-class] processor can do is generating [Gson] adapters.
 */
@Parcelize @DataClass
class ParcelizedType(
    var name: String,
    var age: Int,
    val sex: Int
) : Parcelable {
    override fun toString() = "$name, $sex, $age years old"
}

/**
 * [Parcelize] compatibility tests
 */
@RunWith(AndroidJUnit4::class)
class ParcelizeCompatTest {
    lateinit var gson: Gson

    @Before fun setup() {
        gson = GsonBuilder()
            .registerTypeAdapterFactory(TestTypeAdapterFactory.create())
            .create()
    }

    @Test fun jsonParsing() {
        val json = """{
        |  "name": "Jon",
        |  "age": 16,
        |  "sex": 1
        |}""".trimMargin()

        val data = gson.fromJson(json, ParcelizedType::class.java)
        assertEquals("Jon", data.name)
        assertEquals(16, data.age)
        assertEquals(1, data.sex)
    }

    @Test fun testUnmarshalling() {
        val parcel = Parcel.obtain()
        val person = ParcelizedType("Jane", 21, 0)

        assertEquals("Jane, 0, 21 years old", person.toString())

        person.writeToParcel(parcel, 0)
        parcel.setDataPosition(0)
    }
}
