package nl.natalya.roomreservation.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import nl.natalya.roomreservation.data.Location
import nl.natalya.roomreservation.databinding.LocationItemBinding

class LocationAdapter(private val onClickListener: OnClickListener): ListAdapter<Location, LocationAdapter.LocationViewHolder>(DiffCallback) {
    class LocationViewHolder(private var binding: LocationItemBinding): RecyclerView.ViewHolder(binding.root) {
        fun bind(location: Location){
            binding.location = location
            binding.executePendingBindings()
        }
    }

    companion  object DiffCallback: DiffUtil.ItemCallback<Location>() {
        override fun areItemsTheSame(oldItem: Location, newItem: Location): Boolean {
           return oldItem == newItem
        }

        override fun areContentsTheSame(oldItem: Location, newItem: Location): Boolean {
            return oldItem.locationId == newItem.locationId
        }
    }

    override fun onBindViewHolder(holder: LocationViewHolder, position: Int) {
        val location = getItem(position)
        holder.itemView.setOnClickListener {
            onClickListener.onClick(location)
        }
        holder.bind(location)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LocationViewHolder {
       return LocationViewHolder(LocationItemBinding.inflate(LayoutInflater.from(parent.context)))
    }

    class OnClickListener(val clickListener: (location: Location) -> Unit) {
        fun onClick(location: Location) = clickListener(location)
    }
}