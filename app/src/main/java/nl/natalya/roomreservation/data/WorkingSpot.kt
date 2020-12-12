package nl.natalya.roomreservation.data

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import kotlinx.android.parcel.RawValue

@Parcelize
data class WorkingSpot(
        var workingSpotId: Int,
        var locationId: Int,
        var location: Location,
        var workingSpotReservations: @RawValue MutableList<WorkingSpotReservation>,
        var status: STATUS
       ) : Parcelable
