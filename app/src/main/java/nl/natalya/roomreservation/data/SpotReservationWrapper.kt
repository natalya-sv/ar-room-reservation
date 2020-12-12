package nl.natalya.roomreservation.data

import java.lang.Exception

class SpotReservationWrapper {
    var reservation: WorkingSpotReservation? = null
    var httpRequestResultSuccess: Boolean = false
    var exception: Exception? = null
}