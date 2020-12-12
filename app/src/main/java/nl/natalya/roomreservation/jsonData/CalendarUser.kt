package nl.natalya.roomreservation.jsonData

class CalendarUser() {
    var userId: Int = 0
    var name: String = ""
    var surname: String = ""
    var email: String = ""
    var password: String? = ""
    var job: CalendarJob = CalendarJob()
    var jobId: Int = 0
}