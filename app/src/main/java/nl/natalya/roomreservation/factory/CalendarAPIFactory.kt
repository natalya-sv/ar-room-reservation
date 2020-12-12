package nl.natalya.roomreservation.factory

enum class APIFactory { ASP }

class CalendarAPIFactory {

    fun getCalendarAPI(api: APIFactory): CalendarAPI {
        return when (api) {
            APIFactory.ASP -> AspAPI.getInstance()
        }
    }
}