package nl.natalya.roomreservation.newObjectsToSend

import java.time.LocalDate
import java.time.LocalTime

data class NewMeetingRoomReservation (
        var meetingRoomId: Int,
        var meetingDate: LocalDate,
        var startTime: LocalTime,
        var endTime: LocalTime,
        var userId: Int,
        var meetingTopic: String)