package nl.natalya.roomreservation.data

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import kotlinx.android.parcel.RawValue

@Parcelize
data class Location (
        var locationId: Int,
        var city: City,
        var meetingRooms: @RawValue MutableList<MeetingRoom>,
        var workingSpots: @RawValue MutableList<WorkingSpot>): Parcelable