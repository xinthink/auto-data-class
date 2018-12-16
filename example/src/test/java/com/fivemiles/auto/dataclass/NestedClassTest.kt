package com.fivemiles.auto.dataclass

import android.os.Parcelable
import com.google.gson.Gson
import com.google.gson.TypeAdapter

/**
 * Example of nested interfaces
 */
@DataClass interface EnclosingData : Parcelable {
    val name: String
    val nested: NestedData?

    @DataClass interface NestedData : Parcelable {
        val data: Int

        companion object {
            fun create(data: Int = 0): NestedData =
                DC_EnclosingData_NestedData(data)

            fun gsonTypeAdapter(gson: Gson): TypeAdapter<NestedData> =
                DC_EnclosingData_NestedData.GsonTypeAdapter(gson)
        }
    }

    companion object {
        fun typeAdapter(gson: Gson): TypeAdapter<EnclosingData> =
            DC_EnclosingData.GsonTypeAdapter(gson)
    }
}

/**
 * Example of nested data classes
 */
@DataClass data class EnclosingDataCls(
    val name: String,
    val nested: NestedDataCls?,
    val nested1: NestedDataCls1?
) {
    @DataClass data class NestedDataCls(val data: Int)

    /** Nested data class with Gson TypeAdapter factory method */
    @DataClass data class NestedDataCls1(val data: Int) {
        companion object {
            fun gsonAdapter(gson: Gson): TypeAdapter<NestedDataCls1> =
                DC_EnclosingDataCls_NestedDataCls1_GsonTypeAdapter(gson)
        }
    }
}
