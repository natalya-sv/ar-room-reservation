package nl.natalya.roomreservation.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import nl.natalya.roomreservation.R
import nl.natalya.roomreservation.databinding.FragmentRoomReservationBinding
import nl.natalya.roomreservation.factory.APIFactory
import nl.natalya.roomreservation.helpers.MeetingRoomReservationHelper
import nl.natalya.roomreservation.newObjectsToSend.NewMeetingRoomReservation
import nl.natalya.roomreservation.repositories.MeetingRoomRepository
import nl.natalya.roomreservation.ui.meetingRoom.MeetingRoomViewModel
import nl.natalya.roomreservation.ui.meetingRoom.MeetingRoomViewModelFactory
import nl.natalya.roomreservation.viewmodels.UserViewModel
import java.time.LocalDate

class RoomReservationFragment : Fragment() {

    private lateinit var roomReservationBinding: FragmentRoomReservationBinding
    private val sharedUserViewModel: UserViewModel by activityViewModels()
    private lateinit var meetingRoomViewModel: MeetingRoomViewModel
    var meetingDuration: Int = 30

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        roomReservationBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_room_reservation, container, false)
        val dataSource = MeetingRoomRepository.getInstance()
        val viewModelFactory = MeetingRoomViewModelFactory(dataSource)
        meetingRoomViewModel = ViewModelProvider(this, viewModelFactory).get(MeetingRoomViewModel::class.java)

        val meetingRoom = RoomReservationFragmentArgs.fromBundle(requireArguments()).selectedMeetingRoom

        meetingRoomViewModel.roomReservation.observe(viewLifecycleOwner, Observer { wrapper ->
            if(wrapper !=null) {
                if (wrapper.httpRequestResultSuccess) {
                    wrapper.reservation?.let { meetingRoom.reservations.add(it) }
                    showToastMessage("The reservation has been successful!")
                }
                else {
                    showToastMessage("The reservation failed")
                }
            }
        })

        roomReservationBinding.meetingRoomTxt.text = meetingRoom.meetingRoomName
        roomReservationBinding.timeRadioBtn.setOnCheckedChangeListener { group, checkedId ->
            when (checkedId) {
                R.id.thirtyMin -> {
                    meetingDuration = 30
                }
                R.id.sixtyMin -> {
                    meetingDuration = 60
                }
            }
        }
        roomReservationBinding.bookingRoomBtn.setOnClickListener {
            val topic = roomReservationBinding.topicText.text.toString()
            val userId = sharedUserViewModel.currentUser.value!!.userId

            if (topic.isNotBlank()) {
                val startTime = MeetingRoomReservationHelper.getInstance().getStartTime()
                val endTime = MeetingRoomReservationHelper.getInstance().getEndTime(meetingDuration, startTime)
                val bookingIsAllowed = MeetingRoomReservationHelper.getInstance()
                    .isBookingAllowed(meetingRoom.reservations.size, startTime, endTime, meetingRoom)

                if (bookingIsAllowed) {
                    val newReservation = NewMeetingRoomReservation(meetingRoom.meetingRoomId, LocalDate.now(), startTime, endTime, userId, topic)
                    meetingRoomViewModel.makeRoomReservation(newReservation, APIFactory.ASP)
                }
                else {
                    showToastMessage(resources.getString(R.string.booking_failure))
                }
            }
            else {
                showToastMessage("Please fill in the topic of the meeting!")
            }
        }

        return roomReservationBinding.root
    }

    private fun showToastMessage(message: String) {
        activity?.runOnUiThread {
            Toast.makeText(activity, message, Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        meetingRoomViewModel.clearTheModel()
    }

    companion object {
        @JvmStatic
        fun newInstance(param1: String, param2: String) = RoomReservationFragment().apply {
            arguments = Bundle().apply {

            }
        }
    }
}
