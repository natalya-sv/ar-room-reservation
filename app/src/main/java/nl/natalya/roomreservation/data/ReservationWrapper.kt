package nl.natalya.roomreservation.data

import java.lang.Exception

class ReservationWrapper {
    var reservation: MeetingRoomReservation? = null
    var httpRequestResultSuccess: Boolean = false
    var exception: Exception? = null
}