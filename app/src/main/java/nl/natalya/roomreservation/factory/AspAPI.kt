package nl.natalya.roomreservation.factory

import com.google.ar.core.Session
import nl.natalya.roomreservation.ServiceLocator
import nl.natalya.roomreservation.data.*
import nl.natalya.roomreservation.newObjectsToSend.NewMeetingRoomReservation
import nl.natalya.roomreservation.newObjectsToSend.NewUser
import nl.natalya.roomreservation.newObjectsToSend.NewWorkingSpotReservation
import nl.natalya.roomreservation.ui.location.FILTER_STATUS
import org.apache.http.client.utils.URIBuilder
import java.util.concurrent.CompletableFuture

internal class AspAPI : CalendarAPI {
    private val uriBuilder = URIBuilder()
    private var aspBackEnd: AspBackEnd

    init {
        uriBuilder.scheme = "http"
        uriBuilder.host = "localhost:5000/api"
        aspBackEnd = AspBackEnd.getInstance()
        aspBackEnd.addHeader("content-type", "application/json")
    }

    override fun getRoomAvailabilityInformation(image: String): CompletableFuture<MeetingRoomWrapper> {
        uriBuilder.path = "/meetingRooms/image/$image"

        val uri = uriBuilder.build()
        return aspBackEnd.getMeetingRoomInformationByImageName(uri)
    }

    override fun makeRoomReservation(reservation: NewMeetingRoomReservation): CompletableFuture<ReservationWrapper> {
        uriBuilder.path = "/reservations"
        val uri = uriBuilder.build()

        return aspBackEnd.makeRoomReservation(reservation, uri).thenCompose { id ->
            uriBuilder.path = "/reservations/$id"
            val url = uriBuilder.build()
            aspBackEnd.getMeetingRoomReservationById(url)
        }
    }

    override fun getImagesFromDatabase(): CompletableFuture<MutableList<ImageData>> {
        uriBuilder.path = "/meetingRoomImages"
        val uri = uriBuilder.build()

        return aspBackEnd.getAllMeetingRoomImages(uri)
    }

    override fun getUserByUserEmail(userEmail: String, password: String): CompletableFuture<CurrentUser> {
        uriBuilder.path = "/users/email/$userEmail/password/$password"
        val url = uriBuilder.build()

        return aspBackEnd.getUserByEmail(url)
    }

    override fun addImagesToDatabase(session: Session): CompletableFuture<Boolean> {
        val futureResult = CompletableFuture<Boolean>()

        getImagesFromDatabase().thenApply { listOfImageDate ->
            val result = ServiceLocator.getAugmentedImagesDatabase().addImageToImageDatabase(session, listOfImageDate)
            if (result) {
                futureResult.complete(true)
            }
            else {
                futureResult.completeExceptionally(Throwable())
            }
        }
        return futureResult
    }

    override fun changeUserPassword(userEmail: String, password: String, newPassword: String): CompletableFuture<Boolean> {
        return getUserByUserEmail(userEmail, password).thenCompose { currentUser ->
            uriBuilder.path = "/users/${currentUser.userId}"
            val url = uriBuilder.build()
            aspBackEnd.changePassword(url, newPassword, currentUser)
        }
    }

    override fun createNewAccount(user: NewUser): CompletableFuture<CurrentUser> {
        uriBuilder.path = "/users"
        val url = uriBuilder.build()

        return aspBackEnd.createNewUser(user, url).thenCompose { id->
            uriBuilder.path ="/users/$id"
            val url2 = uriBuilder.build()
            aspBackEnd.getUserById(url2)
        }
    }

    override fun getLocations(filterStatus: FILTER_STATUS): CompletableFuture<MutableList<Location>> {
        uriBuilder.path = "/locations"
        val url = uriBuilder.build()

        return aspBackEnd.getAllLocations(url)
    }

    override fun makeWorkingSpotReservation(spotReservation: NewWorkingSpotReservation): CompletableFuture<SpotReservationWrapper> {
        uriBuilder.path = "/spotReservations"
        val url = uriBuilder.build()

        return aspBackEnd.makeNewWorkingSpotReservation(spotReservation, url).thenCompose { id->
            uriBuilder.path = "/spotReservations/$id"
            val uri = uriBuilder.build()
            aspBackEnd.getSpotReservationById(uri)
        }
    }

    companion object {
        @Volatile
        private var instance: AspAPI? = null

        fun getInstance(): AspAPI {
            synchronized(this) {
                if (instance == null) {
                    instance = AspAPI()
                }
                return instance as AspAPI
            }
        }
    }
}