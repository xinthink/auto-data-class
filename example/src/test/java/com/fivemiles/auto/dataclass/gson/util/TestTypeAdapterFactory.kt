package com.fivemiles.auto.dataclass.gson.util

import com.fivemiles.auto.dataclass.gson.AdaptiveImage
import com.fivemiles.auto.dataclass.gson.FledgedGsonData
import com.fivemiles.auto.dataclass.gson.GsonData
import com.google.gson.Gson
import com.google.gson.TypeAdapter
import com.google.gson.TypeAdapterFactory
import com.google.gson.reflect.TypeToken


/**
 * Created by ywu on 2017/7/18.
 */
class TestTypeAdapterFactory : TypeAdapterFactory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : Any?> create(gson: Gson, type: TypeToken<T>?): TypeAdapter<T>? {
        val rawType = type?.rawType ?: return null
        return when {
            GsonData::class.java.isAssignableFrom(rawType) ->
                GsonData.typeAdapter(gson)
            AdaptiveImage::class.java.isAssignableFrom(rawType) ->
                AdaptiveImage.typeAdapter(gson)
            FledgedGsonData::class.java.isAssignableFrom(rawType) ->
                FledgedGsonData.typeAdapter(gson)
            else -> null
        } as TypeAdapter<T>?
    }
}
