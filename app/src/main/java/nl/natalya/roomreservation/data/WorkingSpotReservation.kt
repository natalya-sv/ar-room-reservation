package nl.natalya.roomreservation.data

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import java.time.LocalDate

@Parcelize
data class WorkingSpotReservation(var workingSpotReservationId: Int,
                                  var reservationDate: LocalDate,
                                  var workingSpot: WorkingSpot,
                                  var user: User): Parcelable
