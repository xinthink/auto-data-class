/*
 * Example model definitions
 *
 * Created by ywu on 2017/6/27.
 */
package com.fivemiles.example.dataclass

import com.fivemiles.auto.dataclass.DataClass
import com.fivemiles.auto.dataclass.DataClassProp

@DataClass interface Address {
    val street: String?
        @DataClassProp(
                jsonField = "street",
                defaultValueLiteral = """"string literal""""
        )
        get

    val city: String

// TODO allow properties with default implement
//    /**
//     * Derived property
//     */
//    val fullAddress: String
//        get() = if (street != null) "$street, $city" else city

    companion object {
        /**  factory method */
        fun create(street: String?, city: String): Address = DC_Address(street, city)
    }
}
