package nl.natalya.roomreservation.jsonData

import nl.natalya.roomreservation.data.STATUS


class CalendarWorkingSpot {

    var workingSpotId: Int=0
    var locationId: Int=0
    var location:CalendarLocation = CalendarLocation()
    var workingSpotReservations: MutableList<CalendarWorkingSpotReservation> = arrayListOf()
    var status: STATUS = STATUS.BUSY
}