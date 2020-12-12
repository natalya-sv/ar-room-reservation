package nl.natalya.roomreservation.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import nl.natalya.roomreservation.R
import nl.natalya.roomreservation.adapters.WorkingSpotAdapter
import nl.natalya.roomreservation.databinding.FragmentLocationDetailsBinding
import nl.natalya.roomreservation.ui.location.LocationDetailsViewModel
import nl.natalya.roomreservation.ui.location.LocationDetailsViewModelFactory
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class LocationDetailsFragment : Fragment() {

    private lateinit var workPlaceDetailsBinding: FragmentLocationDetailsBinding
    private var dateFormat = DateTimeFormatter.ofPattern("dd LLLL yyyy")

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val app = requireNotNull(activity).application
        workPlaceDetailsBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_location_details, container, false)
        workPlaceDetailsBinding.lifecycleOwner = this

        val workPlaceDetails = LocationDetailsFragmentArgs.fromBundle(requireArguments()).selectedLocation
        val viewModelFactory = LocationDetailsViewModelFactory(workPlaceDetails, app)

        workPlaceDetailsBinding.locationDetailsViewModel = ViewModelProvider(this, viewModelFactory).get(LocationDetailsViewModel::class.java)
        val listAdapter = WorkingSpotAdapter()
        workPlaceDetailsBinding.workingSpotsView.adapter = listAdapter

        val today = LocalDate.now()
        val text = today.format(dateFormat)
        workPlaceDetailsBinding.date.text = "Today is: " + text.toString()

        return workPlaceDetailsBinding.root
    }
}

