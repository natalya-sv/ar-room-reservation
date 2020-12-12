package nl.natalya.roomreservation.ui.meetingRoom

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import nl.natalya.roomreservation.data.MeetingRoomWrapper
import nl.natalya.roomreservation.data.ReservationWrapper
import nl.natalya.roomreservation.factory.APIFactory
import nl.natalya.roomreservation.newObjectsToSend.NewMeetingRoomReservation
import nl.natalya.roomreservation.repositories.MeetingRoomRepository

class MeetingRoomViewModel(private val roomRepository: MeetingRoomRepository) : ViewModel() {
    private val _meetingRoomInformation = MutableLiveData<MeetingRoomWrapper>()
    val meetingRoomInformation: LiveData<MeetingRoomWrapper>
        get() = _meetingRoomInformation

    private val _roomReservation: MutableLiveData<ReservationWrapper> = MutableLiveData()
    val roomReservation: LiveData<ReservationWrapper>
        get() = _roomReservation

    fun getRoomAvailabilityInformation(roomImage: String, api: APIFactory) {
        roomRepository.getRoomInformation(roomImage, _meetingRoomInformation, api)
    }

    fun makeRoomReservation(reservation: NewMeetingRoomReservation, api: APIFactory){
        roomRepository.makeRoomReservation(reservation, _roomReservation, api)
    }

    override fun onCleared() {
        super.onCleared()
        _meetingRoomInformation.value = null
        _roomReservation.value = null
    }

    fun clearTheModel(){
        this.onCleared()
    }
}