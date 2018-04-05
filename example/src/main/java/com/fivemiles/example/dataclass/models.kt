/*
 * Example model definitions
 *
 * Created by ywu on 2017/6/27.
 */
package com.fivemiles.example.dataclass

import com.fivemiles.auto.dataclass.DataClass
import com.fivemiles.auto.dataclass.DataProp
import com.google.gson.Gson
import com.google.gson.TypeAdapter
import java.util.Date

@DataClass interface Address {
    @get:DataProp(jsonField = "street", defaultValueLiteral = """"string literal"""")
    val street: String?

    val city: String

    /**
     * Derived property
     */
    val fullAddress: String
        get() = if (street != null) "$street, $city" else city

    companion object {
        /**  factory method */
        fun create(street: String?, city: String): Address = DC_Address(street, city)

        /** Gson TypeAdapter factory method */
        fun typeAdapter(gson: Gson): TypeAdapter<Address> = DC_Address.GsonTypeAdapter(gson)
            .apply {
                defaultCity = "Beijing"
            }
    }
}

@DataClass interface Person {

    val name: String

    @get:DataProp("gender", defaultValueLiteral = "0")
    val gender: Int

    val dateOfBirth: Date

    val address: Address?
}
