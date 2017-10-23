package com.fivemiles.auto.dataclass.gson

import com.google.gson.TypeAdapterFactory

@GsonTypeAdapterFactory object TestTypeAdapterFactory {
    fun create(): TypeAdapterFactory = DC_TestTypeAdapterFactory()
}
