/*
 * Model definitions
 *
 * Created by ywu on 2017/6/27.
 */
package io.github.xinthink.example.dataclass

import io.github.xinthink.auto.dataclass.AutoDataClass
import java.util.*

@AutoDataClass data class Address(val street: String,
                                  val city: String)

@AutoDataClass data class Person(val name: String,
                                 val gender: Int = 1,
                                 val age: Int = 0,
                                 val dateOfBord: Date?,
                                 val address: Address?)
