package nl.natalya.roomreservation.factory

import android.graphics.BitmapFactory
import android.util.Base64
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import nl.natalya.roomreservation.BackEndException
import nl.natalya.roomreservation.BackEndExceptionWithCause
import nl.natalya.roomreservation.data.*
import nl.natalya.roomreservation.jsonData.*
import nl.natalya.roomreservation.newObjectsToSend.NewMeetingRoomReservation
import nl.natalya.roomreservation.newObjectsToSend.NewUser
import nl.natalya.roomreservation.newObjectsToSend.NewWorkingSpotReservation
import org.apache.http.client.methods.HttpGet
import org.apache.http.client.methods.HttpPost
import org.apache.http.client.methods.HttpPut
import org.apache.http.entity.StringEntity
import org.apache.http.impl.client.CloseableHttpClient
import org.apache.http.impl.client.HttpClients
import org.apache.http.util.EntityUtils
import java.io.IOException
import java.net.URI
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.concurrent.CompletableFuture

class AspBackEnd {
    private val gson = Gson()
    lateinit var headerName: String
    lateinit var headerValue: String

    private val timeFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm")

    private var logInFailedException = "Login failed. Check Internet connection."
    private val credentialDoNotMatch = "Password or email does not match"
    private val userAlreadyExists = "The user with such email already exists!"
    private val somethingWentWrong = "Something went wrong"
    private val client: CloseableHttpClient = HttpClients.createDefault()

    fun addHeader(name: String, value: String) {
        headerName = name
        headerValue = value
    }

    //return a list of images for the image database, to scan images
    fun getAllMeetingRoomImages(url: URI): CompletableFuture<MutableList<ImageData>> {
        var roomImages: MutableList<MeetingRoomImage>

        val bitmaps = arrayListOf<ImageData>()
        val type = object : TypeToken<MutableList<MeetingRoomImage>>() {}.type

        return CompletableFuture.supplyAsync {
            try {
                val get = HttpGet(url)
                val httpResponse = client.execute(get)

                httpResponse.use { response ->
                    val responseResult = EntityUtils.toString(response.entity, "UTF-8")
                    roomImages = gson.fromJson(responseResult, type)

                    for (image in roomImages) {
                        val encodeByte: ByteArray = Base64.decode(image.image, Base64.DEFAULT)
                        val meetingRoomImage = MeetingRoomImage(image.meetingRoomImageId, image.name, image.image, image.imageWidth, image.meetingRoomId)
                        val bitmap = BitmapFactory.decodeByteArray(encodeByte, 0, encodeByte.size)
                        val imageData = ImageData(bitmap, meetingRoomImage.name, meetingRoomImage.imageWidth)
                        bitmaps.add(imageData)
                    }
                }
                bitmaps
            } catch (e: IOException) {
                throw BackEndException(logInFailedException)
            }
            catch (e: Exception){
                throw BackEndException(e.cause?.message.toString())
            }
        }
    }

    //get meeting room info by image name
    fun getMeetingRoomInformationByImageName(url: URI): CompletableFuture<MeetingRoomWrapper> {
        return CompletableFuture.supplyAsync {
            try {
                val get = HttpGet(url)
                val wrapper = MeetingRoomWrapper()
                val httpResponse = client.execute(get)
                httpResponse.use { response ->
                    val responseResult = EntityUtils.toString(response.entity, "UTF-8")

                    if (response.statusLine.statusCode == 200) {
                        val calendarMeetingRoom = gson.fromJson(responseResult, CalendarMeetingRoom::class.java)
                        val meetingRoom = createMeetingRoomFromJson(calendarMeetingRoom)
                        wrapper.meetingRoom = meetingRoom
                        wrapper.httpRequestResultSuccess = true
                        wrapper.exception = null
                    }
                    else {
                        wrapper.meetingRoom = null
                        wrapper.httpRequestResultSuccess = false
                        wrapper.exception = Exception("The room cannot be recognized...")
                        throw BackEndException("The room cannot be recognized...")
                    }
                }
                wrapper
            } catch (e: IOException) {
                throw BackEndException(logInFailedException)
            }
        }
    }

    //create a  meeting room information from json
    private fun createMeetingRoomFromJson(calendarMeetingRoom: CalendarMeetingRoom): MeetingRoom {
        val city = City(calendarMeetingRoom.location.city.cityId, calendarMeetingRoom.location.city.cityName)
        val location = Location(calendarMeetingRoom.locationId, city, mutableListOf(), mutableListOf())

        val meetingRoom = MeetingRoom(calendarMeetingRoom.meetingRoomId, calendarMeetingRoom.meetingRoomName, calendarMeetingRoom.meetingRoomEmail, location,
                                      arrayListOf(), STATUS.FREE)
        if (calendarMeetingRoom.meetingRoomReservations.size > 0) {
            for (reservation in calendarMeetingRoom.meetingRoomReservations) {

                val dateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
                val date = LocalDate.parse(reservation.meetingDate, dateTimeFormatter)

                val timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss")
                val startTime = LocalTime.parse(reservation.startTime,timeFormatter)
                val endTime = LocalTime.parse(reservation.endTime, timeFormatter)

                val job = Job(reservation.user?.job!!.jobId, reservation.user!!.job.jobTitle)
                val user = User(reservation.userId, reservation.user!!.name, reservation.user!!.surname, reservation.user!!.email, job)

                val meetingRoomReservation = MeetingRoomReservation(date, LocalTime.parse(startTime.format(timeFormatter)),
                                                                    LocalTime.parse(endTime.format(timeFormatter)), reservation.meetingTopic, user, meetingRoom)
                meetingRoom.reservations.add(meetingRoomReservation)
            }
        }
        return meetingRoom
    }

    //make a new room reservation
    fun makeRoomReservation(reservation: NewMeetingRoomReservation, url: URI): CompletableFuture<Int> {
        val postReservation = createNewReservation(reservation)
        return CompletableFuture.supplyAsync {
            try {
                val post = HttpPost(url)
                var meetingRoomReservationId = 0
                val json = gson.toJson(postReservation)
                val entity = StringEntity(json)
                post.addHeader("content-type", "application/json;charset=utf-8")
                post.entity = entity

                val httpResponse = client.execute(post)
                httpResponse.use { response ->
                    if (response.statusLine.statusCode == 201) {
                        val responseResult = EntityUtils.toString(response.entity, "UTF-8")
                        val cal = gson.fromJson(responseResult, CalendarReservation::class.java)
                        meetingRoomReservationId = cal.meetingRoomReservationId
                    }
                }
                meetingRoomReservationId
            } catch (e: IOException) {
                throw BackEndException(somethingWentWrong)
            }
        }
    }


    //get room information by id
    fun getMeetingRoomReservationById(url: URI): CompletableFuture<ReservationWrapper> {
        var calendarReservation = CalendarReservation()
        val wrapper = ReservationWrapper()
        return CompletableFuture.supplyAsync {
            try {
                val get = HttpGet(url)
                val httpResponse = client.execute(get)
                httpResponse.use { response ->
                    val responseResult = EntityUtils.toString(response.entity, "UTF-8")
                    calendarReservation = gson.fromJson(responseResult, CalendarReservation::class.java)

                    if (response.statusLine.statusCode == 200) {
                        val meetingRoomReservation = createMeetingRoomReservationFromJson(calendarReservation)
                        wrapper.exception = null
                        wrapper.httpRequestResultSuccess = true
                        wrapper.reservation = meetingRoomReservation
                    }
                    else {
                        wrapper.exception = Exception("Reservation not found!")
                        wrapper.httpRequestResultSuccess = false
                        wrapper.reservation = null
                    }
                }
                wrapper
            } catch (e: IOException) {
                throw BackEndException(somethingWentWrong)
            }
        }
    }

    //create meeting room reservation from json
    private fun createMeetingRoomReservationFromJson(calendarReservation: CalendarReservation): MeetingRoomReservation {
        val dateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
        val date = LocalDate.parse(calendarReservation.meetingDate, dateTimeFormatter)

        val timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss")
        val startTime = LocalTime.parse(calendarReservation.startTime,timeFormatter)
        val endTime = LocalTime.parse(calendarReservation.endTime, timeFormatter)

        val job = Job(calendarReservation.user?.job!!.jobId, calendarReservation.user!!.job.jobTitle)
        val city = City(calendarReservation.meetingRoom.location.city.cityId, calendarReservation.meetingRoom.location.city.cityName)
        val user = User(calendarReservation.userId, calendarReservation.user!!.name, calendarReservation.user!!.surname, calendarReservation.user!!.email, job)
        val location = Location(calendarReservation.meetingRoom.location.locationId, city, arrayListOf(), arrayListOf())
        val meetingRoom = MeetingRoom(calendarReservation.meetingRoomId, calendarReservation.meetingRoom.meetingRoomName,
                                      calendarReservation.meetingRoom.meetingRoomEmail, location, arrayListOf(), STATUS.FREE)
        return MeetingRoomReservation(date, startTime, endTime, calendarReservation.meetingTopic, user, meetingRoom)
    }

    //create new reservation  object
    private fun createNewReservation(reservation: NewMeetingRoomReservation): CalendarReservation {
        val postReservation = CalendarReservation()
        postReservation.meetingDate = reservation.meetingDate.toString()
        postReservation.startTime = reservation.startTime.toString()
        postReservation.endTime = reservation.endTime.toString()
        postReservation.userId = reservation.userId
        postReservation.meetingRoomId = reservation.meetingRoomId
        postReservation.meetingTopic = reservation.meetingTopic

        return postReservation
    }

    //return current user
    fun getUserByEmail(url: URI): CompletableFuture<CurrentUser> {
        return CompletableFuture.supplyAsync {
            try {
                val get = HttpGet(url)
              lateinit  var currentUser: CurrentUser
                val httpResponse = client.execute(get)

                httpResponse.use { response ->
                    when (response.statusLine.statusCode) {
                        200 -> {
                            val calendarUser = EntityUtils.toString(response.entity, "UTF-8")
                            val user = gson.fromJson(calendarUser, CalendarUser::class.java)
                            val job = Job(user.job.jobId, user.job.jobTitle)
                            currentUser = CurrentUser(user.userId, user.name, user.email, user.surname, job)
                            CurrentUser.setUser(currentUser)
                        }
                        404 -> throw BackEndException(credentialDoNotMatch)
                        500 -> throw BackEndException("Server Error! Try again")
                        else -> throw BackEndException(somethingWentWrong)
                    }
                }
                currentUser
            } catch (e: IOException) {
                throw BackEndExceptionWithCause(logInFailedException, e)
            }
        }
    }

    //change password of the current user
    fun changePassword(url: URI, newPassword: String, user: CurrentUser): CompletableFuture<Boolean> {
        val currentUser = createCalendarUser(user, newPassword)

        return CompletableFuture.supplyAsync {
            try {
                val put = HttpPut(url)
                var result = false
                val json = gson.toJson(currentUser)
                val entity = StringEntity(json)

                put.addHeader("content-type", "application/json;charset=utf-8")
                put.entity = entity

                val httpResponse = client.execute(put)
                httpResponse.use { response ->
                    when (response.statusLine.statusCode) {
                        204 -> result = true
                        400 -> throw BackEndException(credentialDoNotMatch)
                        500 -> throw BackEndException(somethingWentWrong)
                        else -> throw BackEndException(somethingWentWrong)
                    }
                }
                result
            } catch (e: IOException) {
                throw BackEndExceptionWithCause(logInFailedException, e)
            }
        }
    }

    //create calendar user object
    private fun createCalendarUser(user: CurrentUser, newPassword: String): CalendarUser {
        val currentUser = CalendarUser()
        currentUser.userId = user.userId
        currentUser.email = user.userEmail

        val job = CalendarJob()
        job.jobId = user.job.jobId
        job.jobTitle = user.job.jobTitle

        currentUser.job = job
        currentUser.name = user.userName
        currentUser.surname = user.surname
        currentUser.password = newPassword
        currentUser.jobId = job.jobId

        return currentUser
    }

    //make a new room reservation
    fun createNewUser(newUser: NewUser, url: URI): CompletableFuture<Int> {
        return CompletableFuture.supplyAsync {
            try {
                val post = HttpPost(url)
                var createdUserId = 0
                val json = gson.toJson(newUser)
                val entity = StringEntity(json)
                post.addHeader("content-type", "application/json;charset=utf-8")
                post.entity = entity

                val httpResponse = client.execute(post)
                httpResponse.use { response ->
                    if (response.statusLine.statusCode == 201) {
                        val responseResult = EntityUtils.toString(response.entity, "UTF-8")
                        val cal = gson.fromJson(responseResult, CalendarUser::class.java)
                        createdUserId = cal.userId
                    }
                }
                createdUserId
            } catch (e: IOException) {
                throw BackEndException(somethingWentWrong)
            }
        }
    }

//get user by ID
    fun getUserById (url: URI): CompletableFuture<CurrentUser>{
        var responseResult: String
        var currentUser: CurrentUser? = null
        var retrievedUser: User

        return CompletableFuture.supplyAsync {
            try {
                val get = HttpGet(url)
                val httpResponse = client.execute(get)
                httpResponse.use { response ->
                    responseResult = EntityUtils.toString(response.entity, "UTF-8")
                    val user = gson.fromJson(responseResult, CalendarUser::class.java)
                    val job = Job(user.job.jobId, user.job.jobTitle)
                    retrievedUser = User(user.userId, user.name, user.surname, user.email, job)

                    when (response.statusLine.statusCode) {
                        200 -> currentUser = CurrentUser(retrievedUser.userId, retrievedUser.name, retrievedUser.email, retrievedUser.surname, job)
                        400 -> {
                            currentUser = null
                            throw BackEndException(userAlreadyExists)
                        }
                        else -> {
                            currentUser = null
                            throw BackEndException(somethingWentWrong)
                        }
                    }
                }
                currentUser
            } catch (e: IOException) {
                throw BackEndException(somethingWentWrong)
            }
        }
    }

    //get all locations
    fun getAllLocations(url: URI): CompletableFuture<MutableList<Location>> {
        var locations: MutableList<Location> = mutableListOf<Location>()
        val type = object : TypeToken<MutableList<CalendarLocation>>() {}.type
        var jsonResponse: MutableList<CalendarLocation> = mutableListOf<CalendarLocation>()

        return CompletableFuture.supplyAsync {
            try {
                val get = HttpGet(url)
                val httpResponse = client.execute(get)
                httpResponse.use { response ->

                    val responseResult = EntityUtils.toString(response.entity, "UTF-8")
                    jsonResponse = gson.fromJson(responseResult, type)
                    if (response.statusLine.statusCode == 200) {
                        locations = createListOfLocations(jsonResponse)
                    }
                }
                locations
            } catch (e: IOException) {
                throw BackEndException("Error..")
            }
        }
    }


    //crete list of locations from json
    private fun createListOfLocations(list: MutableList<CalendarLocation>): MutableList<Location>{

        val locations = mutableListOf<Location>()
        for (workingLocation in list) {
            val city = City(workingLocation.city.cityId, workingLocation.city.cityName)
            val location = Location(workingLocation.locationId, city, mutableListOf(), mutableListOf())
            if (workingLocation.workingSpots.size > 0) {
                for (workingSpot in workingLocation.workingSpots) {
                    val spot = WorkingSpot(workingSpot.workingSpotId, workingSpot.locationId, location, mutableListOf(), STATUS.FREE)
                    if (workingSpot.workingSpotReservations.size > 0) {
                        for (workingSpotReservation in workingSpot.workingSpotReservations) {
                            val job = Job(workingSpotReservation.user.job.jobId, workingSpotReservation.user.job.jobTitle)
                            val user = User(workingSpotReservation.user.userId, workingSpotReservation.user.name,
                                            workingSpotReservation.user.surname, workingSpotReservation.user.email, job)

                            val dateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
                            val date = LocalDate.parse(workingSpotReservation.reservationDate, dateTimeFormatter)

                            if (date == LocalDate.now()) {
                                spot.status = STATUS.BUSY
                                val reservation = WorkingSpotReservation(workingSpotReservation.workingSpotReservationId, date,
                                                                                                   WorkingSpot(workingSpot.workingSpotId, workingSpot.locationId, location,
                                                                                                               arrayListOf(),  STATUS.FREE), user)
                                spot.workingSpotReservations.add(reservation)
                            }
                        }
                    }
                    location.workingSpots.add(spot)
                }
            }
            if (workingLocation.meetingRooms.size > 0) {
                for (meetingRoom in workingLocation.meetingRooms) {
                    val room = MeetingRoom(meetingRoom.meetingRoomId, meetingRoom.meetingRoomName, meetingRoom.meetingRoomEmail, location,
                                           arrayListOf(), STATUS.FREE)
                    location.meetingRooms.add(room)
                }
            }
            locations.add(location)
        }
    return locations
    }

//create new reservation for working spot
    fun makeNewWorkingSpotReservation(spotReservation: NewWorkingSpotReservation, url: URI): CompletableFuture<Int> {
        var workingSpotReservationId = 0
        return CompletableFuture.supplyAsync {
            try {
                val post = HttpPost(url)
                val json = gson.toJson(spotReservation)
                val entity = StringEntity(json)
                post.addHeader("content-type", "application/json;charset=utf-8")
                post.entity = entity

                val httpResponse = client.execute(post)
                httpResponse.use { response ->
                    if (response.statusLine.statusCode == 201) {
                        val responseResult = EntityUtils.toString(response.entity, "UTF-8")
                        val cal = gson.fromJson(responseResult, CalendarWorkingSpotReservation::class.java)
                        workingSpotReservationId = cal.workingSpotReservationId
                    }
                }
                workingSpotReservationId
            } catch (e: IOException) {
                throw BackEndException(logInFailedException)
            }
        }
    }

    //create working spot object from json
    private fun createSpotReservationFromJson(reservation: CalendarWorkingSpotReservation): WorkingSpotReservation{
        val dateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
        val date = LocalDate.parse(reservation.reservationDate, dateTimeFormatter)

        val job = Job(reservation.user.job.jobId, reservation.user.job.jobTitle)
        val city = City(reservation.workingSpot.location.city.cityId, reservation.workingSpot.location.city.cityName)
        val user = User(reservation.userId, reservation.user.name, reservation.user.surname, reservation.user.email, job)
        val location = Location(reservation.workingSpot.location.locationId, city, arrayListOf(), arrayListOf())
        val workingSpot = WorkingSpot(reservation.workingSpot.workingSpotId, reservation.workingSpot.locationId, location, arrayListOf(),status = STATUS.FREE)
        return WorkingSpotReservation(reservation.workingSpotReservationId,date,workingSpot,user)
    }

    //get spot reservation from id
    fun getSpotReservationById(uri:URI): CompletableFuture<SpotReservationWrapper> {
        var calendarWorkingSpotReservation = CalendarWorkingSpotReservation()
        val wrapper = SpotReservationWrapper()
        return CompletableFuture.supplyAsync {
            try {
                val get = HttpGet(uri)
                val httpResponse = client.execute(get)
                httpResponse.use { response ->
                    val responseResult = EntityUtils.toString(response.entity, "UTF-8")
                    calendarWorkingSpotReservation = gson.fromJson(responseResult, CalendarWorkingSpotReservation::class.java)
                    if (response.statusLine.statusCode == 200) {
                        wrapper.exception = null
                        wrapper.httpRequestResultSuccess = true
                        wrapper.reservation = createSpotReservationFromJson(calendarWorkingSpotReservation)
                    }
                    else {
                        wrapper.exception = Exception("Reservation not found!")
                        wrapper.httpRequestResultSuccess = false
                        wrapper.reservation = null
                    }
                }
                wrapper
            } catch (e: IOException) {
                throw BackEndException(somethingWentWrong)
            }
        }
    }

    companion object {
        @Volatile
        private var instance: AspBackEnd? = null

        fun getInstance(): AspBackEnd {
            if (instance == null) {
                instance = AspBackEnd()
            }
            return instance as AspBackEnd
        }
    }
}