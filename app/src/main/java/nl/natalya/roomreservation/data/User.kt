package nl.natalya.roomreservation.data

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class User (
        var userId: Int,
        var name: String,
        var surname: String,
        var email: String,
        var job: Job
):Parcelable