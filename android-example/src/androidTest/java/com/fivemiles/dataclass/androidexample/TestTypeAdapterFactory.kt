package com.fivemiles.dataclass.androidexample

import com.fivemiles.auto.dataclass.gson.GsonTypeAdapterFactory
import com.google.gson.TypeAdapterFactory

@GsonTypeAdapterFactory object TestTypeAdapterFactory {
    fun create(): TypeAdapterFactory = DC_TestTypeAdapterFactory()
}
