package nl.natalya.roomreservation.data

import java.lang.Exception

class MeetingRoomWrapper {
    var httpRequestResultSuccess: Boolean = false
    var meetingRoom: MeetingRoom? = null
    var exception: Exception? = null
}