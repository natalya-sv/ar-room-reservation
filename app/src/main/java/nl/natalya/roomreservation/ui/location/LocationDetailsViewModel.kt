package nl.natalya.roomreservation.ui.location

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import nl.natalya.roomreservation.data.Location
import nl.natalya.roomreservation.data.WorkingSpot
import nl.natalya.roomreservation.factory.APIFactory
import nl.natalya.roomreservation.factory.CalendarAPIFactory

class LocationDetailsViewModel(location: Location, app:Application): AndroidViewModel(app) {

    private val _selectedLocation = MutableLiveData<Location>()
    val selectedLocation: LiveData<Location>
    get() = _selectedLocation

    init {
        _selectedLocation.value = location
    }
}