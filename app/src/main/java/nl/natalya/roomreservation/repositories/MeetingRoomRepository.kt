package nl.natalya.roomreservation.repositories

import androidx.lifecycle.MutableLiveData
import nl.natalya.roomreservation.factory.CalendarAPIFactory
import nl.natalya.roomreservation.data.MeetingRoomReservation
import nl.natalya.roomreservation.data.MeetingRoomWrapper
import nl.natalya.roomreservation.data.ReservationWrapper
import nl.natalya.roomreservation.factory.APIFactory
import nl.natalya.roomreservation.newObjectsToSend.NewMeetingRoomReservation

class MeetingRoomRepository {
       /** Gets room availability information by the room email */
    fun getRoomInformation(roomImage: String, meetingRoomInformation: MutableLiveData<MeetingRoomWrapper>, api: APIFactory) {
        val calendarAPI = CalendarAPIFactory().getCalendarAPI(api)
        calendarAPI.getRoomAvailabilityInformation(roomImage).thenApply {meetingRoom ->
            meetingRoomInformation.postValue(meetingRoom)
        }?.exceptionally {
            meetingRoomInformation.postValue(null)
        }
    }

    /** Makes new reservation */
    fun makeRoomReservation(reservation: NewMeetingRoomReservation, roomReservation: MutableLiveData<ReservationWrapper>, api: APIFactory) {
        val calendarAPI = CalendarAPIFactory().getCalendarAPI(api)
        calendarAPI.makeRoomReservation(reservation).thenApply { createdReservation ->
            roomReservation.postValue(createdReservation)
        }?.exceptionally {
            roomReservation.postValue(null)
        }
    }

    companion object {
        @Volatile
        private var instance: MeetingRoomRepository? = null

        fun getInstance(): MeetingRoomRepository {
            synchronized(this){
                if (instance == null) {
                    instance = MeetingRoomRepository()
                }
                return instance as MeetingRoomRepository
            }
        }
    }
}