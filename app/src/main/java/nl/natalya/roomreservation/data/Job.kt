package nl.natalya.roomreservation.data

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Job (
        var jobId: Int,
        var jobTitle: String
):Parcelable