/*
 * Example model definitions
 *
 * Created by ywu on 2017/6/27.
 */
package com.fivemiles.example.dataclass

import com.fivemiles.auto.dataclass.DataClass
import com.fivemiles.auto.dataclass.DataClassProp
import com.google.gson.Gson
import com.google.gson.TypeAdapter
import java.util.*

@DataClass interface Address {
    val street: String?
        @DataClassProp(
                jsonField = "street",
                defaultValueLiteral = """"string literal""""
        )
        get

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
    val gender: Int @DataClassProp("gender", defaultValueLiteral = "0") get
    val dateOfBirth: Date
    val address: Address?
}
