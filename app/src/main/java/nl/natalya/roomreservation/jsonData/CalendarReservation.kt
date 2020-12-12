package nl.natalya.roomreservation.jsonData

class  CalendarReservation(){
     var meetingRoomReservationId: Int = 0
     var meetingDate: String= ""
     var startTime: String= ""
     var endTime: String= ""
     var meetingTopic: String= ""
     var userId: Int = 0
     var user: CalendarUser? = CalendarUser()
     var meetingRoomId: Int = 0
     var meetingRoom:CalendarMeetingRoom = CalendarMeetingRoom()
 }
