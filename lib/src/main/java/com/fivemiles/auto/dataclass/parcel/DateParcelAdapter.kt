package com.fivemiles.auto.dataclass.parcel

import android.os.Parcel
import java.util.*

/**
 * A [ParcelAdapter] for converting [Date]s from/to [Parcel]s.
 */
class DateParcelAdapter : ParcelAdapter<Date> {

    override fun fromParcel(source: Parcel): Date = Date(source.readLong())

    override fun toParcel(value: Date?, dest: Parcel) {
        if (value != null) {
            dest.writeLong(value.time)
        }
    }

}
