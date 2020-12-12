package nl.natalya.roomreservation.helpers

import nl.natalya.roomreservation.data.MeetingRoom
import java.time.Clock
import java.time.Duration
import java.time.LocalTime
import java.util.*

class MeetingRoomReservationHelper {

    var isTest = false
    private lateinit var clock: Clock

    /** Checks if the new meeting does not interfere with other reservations */
    fun isBookingAllowed(numberOfBookings: Int, startTime: LocalTime, endTime: LocalTime, meetingRoom: MeetingRoom): Boolean {
        return if (numberOfBookings > 0) {
            val difference = Duration.between(startTime, meetingRoom.reservations[0].startTime).toMinutes()
            startTime < meetingRoom.reservations[0].startTime && endTime <= meetingRoom.reservations[0].startTime && difference >= 20
        }
        else {
            true
        }
    }

    /** Rounds the start time  */
    fun getStartTime(): LocalTime {
        val calendar = Calendar.getInstance()
        var currentHour: Int
        val currentMinute: Int

        if (isTest) {
            currentHour = LocalTime.now(clock).hour
            currentMinute = LocalTime.now(clock).minute
        }
        else {
            currentHour = calendar.get(Calendar.HOUR_OF_DAY)
            currentMinute = calendar.get(Calendar.MINUTE)
        }

        var meetingStartMinutes = 0

        when (currentMinute) {
            in 0..15 -> meetingStartMinutes = 0
            in 16..45 -> meetingStartMinutes = 30
            in 46..59 -> {
                currentHour += 1
                meetingStartMinutes = 0
            }
        }

        return LocalTime.of(currentHour, meetingStartMinutes)
    }

    fun setClock(newClock: Clock) {
        clock = newClock
    }

    /** Creates the end time of a meeting */
    fun getEndTime(meetingDuration: Int, start: LocalTime): LocalTime {
        return start.plusMinutes(meetingDuration.toLong())
    }

    companion object {
        private var instance: MeetingRoomReservationHelper? = null

        @Synchronized
        fun getInstance(): MeetingRoomReservationHelper {
            val meetingRoomReservationHelper = instance

            if (meetingRoomReservationHelper != null) {
                return meetingRoomReservationHelper
            }

            val newMeetingRoomReservationHelper = MeetingRoomReservationHelper()
            instance = newMeetingRoomReservationHelper

            return newMeetingRoomReservationHelper
        }
    }
}