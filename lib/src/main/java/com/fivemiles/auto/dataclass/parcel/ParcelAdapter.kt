package com.fivemiles.auto.dataclass.parcel

import android.os.Parcel

/**
 * Custom logic converting objects to and from Parcels.
 */
interface ParcelAdapter<T> {

    /**
     * Creates a new object based on the values in the provided [Parcel].
     * @param source The [Parcel] which contains the values of `T`.
     * @return A new object based on the values in `source`.
     */
    fun fromParcel(source: Parcel): T

    /**
     * Writes `value` into `dest`.
     * @param value The object to be written.
     * @param dest The [Parcel] in which to write `value`.
     */
    fun toParcel(value: T?, dest: Parcel)
}
