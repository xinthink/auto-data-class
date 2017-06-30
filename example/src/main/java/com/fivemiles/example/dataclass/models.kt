/*
 * Example model definitions
 *
 * Created by ywu on 2017/6/27.
 */
package com.fivemiles.example.dataclass

import com.fivemiles.auto.dataclass.DataClass
import com.fivemiles.auto.dataclass.DataClassProp

@DataClass interface Address {
    val street: String? @DataClassProp("street") get
    val city: String
}
