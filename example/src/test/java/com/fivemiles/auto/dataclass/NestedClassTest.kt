package com.fivemiles.auto.dataclass

import android.os.Parcelable
import com.google.gson.Gson
import com.google.gson.TypeAdapter

@DataClass interface EnclosingData : Parcelable {
    val name: String
    val nested: NestedData?

    @DataClass interface NestedData : Parcelable {
        val data: Int

        companion object {
            fun create(data: Int = 0): EnclosingData.NestedData =
                DC_EnclosingData_NestedData(data)

            fun gsonTypeAdapter(gson: Gson): TypeAdapter<EnclosingData.NestedData> =
                DC_EnclosingData_NestedData.GsonTypeAdapter(gson)
        }
    }

    companion object {
        fun typeAdapter(gson: Gson): TypeAdapter<EnclosingData> =
            DC_EnclosingData.GsonTypeAdapter(gson)
    }
}
