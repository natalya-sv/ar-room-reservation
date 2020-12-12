package nl.natalya.roomreservation.factory

import com.google.ar.core.Session
import nl.natalya.roomreservation.data.*
import nl.natalya.roomreservation.newObjectsToSend.NewMeetingRoomReservation
import nl.natalya.roomreservation.newObjectsToSend.NewUser
import nl.natalya.roomreservation.newObjectsToSend.NewWorkingSpotReservation
import nl.natalya.roomreservation.ui.location.FILTER_STATUS
import java.util.concurrent.CompletableFuture

interface CalendarAPI {
    fun getRoomAvailabilityInformation(image: String): CompletableFuture<MeetingRoomWrapper>
    fun makeRoomReservation(reservation: NewMeetingRoomReservation): CompletableFuture<ReservationWrapper>
    fun getImagesFromDatabase():CompletableFuture<MutableList<ImageData>>
    fun getUserByUserEmail(userEmail: String, password: String): CompletableFuture<CurrentUser>
    fun addImagesToDatabase(session: Session): CompletableFuture<Boolean>
    fun changeUserPassword(userEmail: String, password: String, newPassword: String): CompletableFuture<Boolean>
    fun createNewAccount(user: NewUser): CompletableFuture<CurrentUser>
    fun getLocations(filterStatus: FILTER_STATUS): CompletableFuture<MutableList<Location>>
    fun makeWorkingSpotReservation(spotReservation: NewWorkingSpotReservation): CompletableFuture<SpotReservationWrapper>
}