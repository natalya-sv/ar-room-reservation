package nl.natalya.roomreservation.adapters

import androidx.databinding.BindingAdapter
import androidx.recyclerview.widget.RecyclerView
import nl.natalya.roomreservation.data.Location
import nl.natalya.roomreservation.data.WorkingSpot

@BindingAdapter("listData")
fun bindRecyclerView(recyclerView: RecyclerView, data: List<Location>?) {
    val adapter = recyclerView.adapter as LocationAdapter
    adapter.submitList(data)
}

@BindingAdapter("listSpots")
fun bindRecyclerView2(recyclerView: RecyclerView, data: List<WorkingSpot>?) {
    val adapter = recyclerView.adapter as WorkingSpotAdapter
    adapter.submitList(data)

}

