package nl.natalya.roomreservation.data

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class City(
        var cityId: Int,
        var cityName: String
): Parcelable