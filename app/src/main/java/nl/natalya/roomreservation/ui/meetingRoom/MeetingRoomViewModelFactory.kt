package nl.natalya.roomreservation.ui.meetingRoom

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import nl.natalya.roomreservation.repositories.MeetingRoomRepository

class MeetingRoomViewModelFactory(private val roomsRepository: MeetingRoomRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MeetingRoomViewModel::class.java)) {
            return MeetingRoomViewModel(roomsRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}