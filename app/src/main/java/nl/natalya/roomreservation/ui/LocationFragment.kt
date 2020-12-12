package nl.natalya.roomreservation.ui

import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import nl.natalya.roomreservation.R
import nl.natalya.roomreservation.adapters.LocationAdapter
import nl.natalya.roomreservation.databinding.FragmentLocationBinding
import nl.natalya.roomreservation.ui.location.FILTER_STATUS
import nl.natalya.roomreservation.ui.location.LocationsOverviewViewModel

class LocationFragment : Fragment() {
    private lateinit  var locationsBinding: FragmentLocationBinding

    private val locationsOverviewModel: LocationsOverviewViewModel by lazy {
        ViewModelProvider(this).get(LocationsOverviewViewModel::class.java)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        locationsBinding =   FragmentLocationBinding.inflate(inflater)
        locationsBinding.locationViewModel = locationsOverviewModel
        locationsBinding.lifecycleOwner = this

       val listAdapter = LocationAdapter (LocationAdapter.OnClickListener {
           locationsOverviewModel.displaySelectedWorkPlace(it)
       })

        locationsBinding.locationsView.adapter = listAdapter
        setHasOptionsMenu(true)

        locationsOverviewModel.navigateToSelectedLocation.observe(viewLifecycleOwner, Observer {
            if(null != it){
                this.findNavController().navigate(LocationFragmentDirections.actionLocationFragmentToLocationDetailsFragment(it))
                locationsOverviewModel.displayWorkPlaceDetailsComplete()
            }
        })

        return locationsBinding.root
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.overflow_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        locationsOverviewModel.updateFilter(
            when(item.itemId){
                R.id.show_all_menu -> FILTER_STATUS.ALL
                R.id.show_by_busy_status -> FILTER_STATUS.BUSY
                R.id.show_by_free_status -> FILTER_STATUS.FREE
                else -> FILTER_STATUS.ALL
            }
        )
        return true
    }
}