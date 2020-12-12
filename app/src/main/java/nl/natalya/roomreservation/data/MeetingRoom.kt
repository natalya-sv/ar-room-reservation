package nl.natalya.roomreservation.data

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class MeetingRoom(
        var meetingRoomId: Int,
        var meetingRoomName: String,
        var meetingRoomEmail: String,
        var location: Location,
        var reservations: MutableList<MeetingRoomReservation>,
        var currentStatus: STATUS
):Parcelable