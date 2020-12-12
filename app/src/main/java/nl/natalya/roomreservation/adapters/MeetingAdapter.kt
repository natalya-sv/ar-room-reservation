package nl.natalya.roomreservation.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import nl.natalya.roomreservation.R
import nl.natalya.roomreservation.data.MeetingRoomReservation

class MeetingAdapter(context: Context, meetings: MutableList<MeetingRoomReservation>) :
        ArrayAdapter<MeetingRoomReservation>(context, 0, meetings) {
    /** Prepares list of reservations to be displayed */
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var meetingInformation = convertView
        if (meetingInformation == null) {
            meetingInformation = LayoutInflater.from(context).inflate(R.layout.item_meeting_view, parent, false)
        }
        val currentMeeting: MeetingRoomReservation? = getItem(position)

        val start: TextView? = meetingInformation?.findViewById(R.id.start_time_meeting)
        start?.text = currentMeeting?.startTime.toString()

        val hyphen: TextView? = meetingInformation?.findViewById(R.id.timeHyphen)
        hyphen?.text = "-"

        val end: TextView? = meetingInformation?.findViewById(R.id.end_time_meeting)
        end?.text = currentMeeting?.endTime.toString()

        val colon: TextView? = meetingInformation?.findViewById(R.id.colon)
        colon?.text = " -"

        val organizer: TextView? = meetingInformation?.findViewById(R.id.meeting_organizer)
        organizer?.text = currentMeeting?.user?.name.plus(" ").plus(currentMeeting?.user?.surname) ?: ""

        val topic: TextView? = meetingInformation?.findViewById(R.id.meetingTopicText)
        topic?.text = currentMeeting?.meetingTopic ?: ""


        return meetingInformation!!
    }
}