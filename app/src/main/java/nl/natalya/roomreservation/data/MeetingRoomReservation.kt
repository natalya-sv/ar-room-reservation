package nl.natalya.roomreservation.data

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import java.time.LocalDate
import java.time.LocalTime

@Parcelize
data class MeetingRoomReservation(
        var meetingDate: LocalDate,
        var startTime: LocalTime,
        var endTime: LocalTime,
        var meetingTopic: String,
        var user: User,
        var meetingRoom: MeetingRoom): Parcelable