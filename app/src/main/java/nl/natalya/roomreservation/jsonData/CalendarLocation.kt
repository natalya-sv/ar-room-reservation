package nl.natalya.roomreservation.jsonData

import nl.natalya.roomreservation.data.MeetingRoom
import nl.natalya.roomreservation.data.WorkingSpot

class CalendarLocation() {
    var locationId: Int=0
    var city: CalendarCity = CalendarCity()
    var meetingRooms:  MutableList<CalendarMeetingRoom> = arrayListOf()
    var workingSpots: MutableList<CalendarWorkingSpot> = arrayListOf()
}