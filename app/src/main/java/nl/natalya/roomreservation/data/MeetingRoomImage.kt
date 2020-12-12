package nl.natalya.roomreservation.data

data class MeetingRoomImage(
        var meetingRoomImageId: Int,
        var name: String,
        var image:String,
        var imageWidth: Double,
        var meetingRoomId: Int
)