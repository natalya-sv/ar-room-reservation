package nl.natalya.roomreservation.jsonData

 class CalendarMeetingRoom() {
   var meetingRoomId: Int = 0
   var meetingRoomName: String = ""
   var meetingRoomEmail: String = ""
   var locationId: Int = 1
   var location: CalendarLocation = CalendarLocation()
   var meetingRoomReservations: MutableList<CalendarReservation> = arrayListOf()
   var meetingRoomImage: CalendarMeetingRoomImage = CalendarMeetingRoomImage()
}