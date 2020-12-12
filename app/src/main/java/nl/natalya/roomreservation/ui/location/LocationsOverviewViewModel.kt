package nl.natalya.roomreservation.ui.location

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import nl.natalya.roomreservation.data.Location
import nl.natalya.roomreservation.factory.APIFactory
import nl.natalya.roomreservation.factory.CalendarAPIFactory

enum class FILTER_STATUS {
    ALL, FREE, BUSY
}

class LocationsOverviewViewModel : ViewModel() {

    private val _listOfLocations = MutableLiveData<MutableList<Location>>()
    val listOfLocations: LiveData<MutableList<Location>>
        get() = _listOfLocations

    private val _navigateToSelectedLocation = MutableLiveData<Location>()
    val navigateToSelectedLocation: LiveData<Location>
        get() = _navigateToSelectedLocation

    init {
        getAllLocations(FILTER_STATUS.ALL)
    }

    fun displaySelectedWorkPlace(spot: Location) {
        _navigateToSelectedLocation.value = (spot)
    }

    fun displayWorkPlaceDetailsComplete() {
        _navigateToSelectedLocation.value = (null)
    }

    private fun getAllLocations(filter: FILTER_STATUS) {
      CalendarAPIFactory().getCalendarAPI(APIFactory.ASP).getLocations(filter).thenApply {
            _listOfLocations.postValue(it)
        }?.exceptionally {
            _listOfLocations.postValue(null)
        }
    }

    fun updateFilter(filter: FILTER_STATUS) {

    }

    override fun onCleared() {
        super.onCleared()
        _listOfLocations.value = null
    }
}