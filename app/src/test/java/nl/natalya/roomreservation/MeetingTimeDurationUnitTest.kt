package nl.natalya.roomreservation

import io.mockk.every
import io.mockk.mockk
import nl.natalya.roomreservation.helpers.MeetingRoomReservationHelper
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.*

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class MeetingTimeDurationUnitTest {
    private val shortMeetingDuration = 30
    private val longMeetingDuration = 60

    @Test
    fun getStartTime_When_Minutes_Between_Zero_And_Fifteen() {
        val timeInstance = LocalDateTime.of(2020,5,15,13,12).toInstant(ZoneOffset.UTC)
        val clock = mockk<Clock>()
        every { clock.instant() } returns timeInstance
        every { clock.zone } returns  ZoneId.of("UTC")
        MeetingRoomReservationHelper.getInstance().isTest = true
        MeetingRoomReservationHelper.getInstance().setClock(clock)
        val result = MeetingRoomReservationHelper.getInstance().getStartTime()
        assertEquals(LocalTime.of(13,0), result)
    }

    @Test
    fun getStartTime_When_Minutes_Between_Sixteen_And_Thirty() {
        val timeInstance = LocalDateTime.of(2020,5,25,13,18).toInstant(ZoneOffset.UTC)
        val clock = mockk<Clock>()
        every { clock.instant() } returns timeInstance
        every { clock.zone } returns  ZoneId.of("UTC")
        MeetingRoomReservationHelper.getInstance().isTest = true
        MeetingRoomReservationHelper.getInstance().setClock(clock)
        val result = MeetingRoomReservationHelper.getInstance().getStartTime()
        assertEquals(LocalTime.of(13, 30), result)
    }

    @Test
    fun getStartTime_When_Minutes_Between_ThirtyOne_And_FortyFive() {
        val timeInstance = LocalDateTime.of(2020,5,25,12,38).toInstant(ZoneOffset.UTC)
        val clock = mockk<Clock>()
        every { clock.instant() } returns timeInstance
        every { clock.zone } returns  ZoneId.of("UTC")
        MeetingRoomReservationHelper.getInstance().isTest = true
        MeetingRoomReservationHelper.getInstance().setClock(clock)
        val result = MeetingRoomReservationHelper.getInstance().getStartTime()
        assertEquals(LocalTime.of(12, 30), result)
    }

    @Test
    fun getStartTime_When_Minutes_Between_FortySix_And_FiftyNine() {
        val timeInstance = LocalDateTime.of(2020,5,25,13,48).toInstant(ZoneOffset.UTC)
        val clock = mockk<Clock>()
        every { clock.instant() } returns timeInstance
        every { clock.zone } returns  ZoneId.of("UTC")
        MeetingRoomReservationHelper.getInstance().isTest = true
        MeetingRoomReservationHelper.getInstance().setClock(clock)
        val result = MeetingRoomReservationHelper.getInstance().getStartTime()
        assertEquals(LocalTime.of(14, 0), result)
    }

    @Test
    fun getEndTime_With_ShortDuration() {
        val timeInstance = LocalDateTime.of(2020,5,25,13,48).toInstant(ZoneOffset.UTC)
        val clock = mockk<Clock>()
        every { clock.instant() } returns timeInstance
        every { clock.zone } returns  ZoneId.of("UTC")
        val start = MeetingRoomReservationHelper.getInstance().getStartTime()
        val result = MeetingRoomReservationHelper.getInstance().getEndTime(shortMeetingDuration, start)
        assertEquals(start.plusMinutes(shortMeetingDuration.toLong()), result)
    }

    @Test
    fun getEndTime_With_LongDuration() {
        val timeInstance = LocalDateTime.of(2020,5,25,13,48).toInstant(ZoneOffset.UTC)
        val clock = mockk<Clock>()
        every { clock.instant() } returns timeInstance
        every { clock.zone } returns  ZoneId.of("UTC")

        val start = MeetingRoomReservationHelper.getInstance().getStartTime()
        val result = MeetingRoomReservationHelper.getInstance().getEndTime(longMeetingDuration, start)
        assertEquals(start.plusMinutes(longMeetingDuration.toLong()), result)
    }
}
