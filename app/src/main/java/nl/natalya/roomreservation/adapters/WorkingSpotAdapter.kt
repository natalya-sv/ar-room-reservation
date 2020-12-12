package nl.natalya.roomreservation.adapters

import android.app.Activity
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.working_spot_item.view.*
import nl.natalya.roomreservation.R
import nl.natalya.roomreservation.data.CurrentUser
import nl.natalya.roomreservation.data.STATUS
import nl.natalya.roomreservation.data.WorkingSpot
import nl.natalya.roomreservation.databinding.WorkingSpotItemBinding
import nl.natalya.roomreservation.factory.APIFactory
import nl.natalya.roomreservation.factory.CalendarAPIFactory
import nl.natalya.roomreservation.newObjectsToSend.NewWorkingSpotReservation
import java.time.LocalDate


class WorkingSpotAdapter() : ListAdapter<WorkingSpot, WorkingSpotAdapter.WorkingSpotViewHolder>(DiffCallback) {

    class WorkingSpotViewHolder(private var binding: WorkingSpotItemBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(workingSpot: WorkingSpot) {
            binding.spot = workingSpot
            binding.executePendingBindings()
        }
    }

    companion object DiffCallback : DiffUtil.ItemCallback<WorkingSpot>() {
        override fun areItemsTheSame(oldItem: WorkingSpot, newItem: WorkingSpot): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(oldItem: WorkingSpot, newItem: WorkingSpot): Boolean {
            return oldItem.locationId == newItem.locationId
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WorkingSpotViewHolder {
        return WorkingSpotAdapter.WorkingSpotViewHolder(WorkingSpotItemBinding.inflate(LayoutInflater.from(parent.context)))
    }

    override fun onBindViewHolder(holder: WorkingSpotViewHolder, position: Int) {
        val spot = getItem(position)
        if (spot.status == STATUS.BUSY) {
            holder.itemView.spotReservationBtn.isEnabled = false
            holder.itemView.spotReservationBtn.setBackgroundResource(R.drawable.gray_round_button)
        }
        holder.itemView.spotReservationBtn.setOnClickListener {
            val reservationDate = LocalDate.now()
            val reservation = NewWorkingSpotReservation(spot!!.workingSpotId, reservationDate.toString(), CurrentUser.getUser()!!.userId)
            CalendarAPIFactory().getCalendarAPI(APIFactory.ASP).makeWorkingSpotReservation(reservation).thenApply {
                (holder.itemView.context as Activity).runOnUiThread {
                    if (it.httpRequestResultSuccess) {
                        holder.itemView.spotReservationBtn.setBackgroundResource(R.drawable.gray_round_button)
                        holder.itemView.spotReservationBtn.isClickable = false
                        it.reservation?.workingSpot?.status = STATUS.BUSY
                        spot.status = STATUS.BUSY
                        holder.itemView.workingSpotStatus.text = STATUS.BUSY.toString()
                    }
                    else {
                        holder.itemView.spotReservationBtn.setBackgroundResource(R.drawable.blue_round_button)
                        holder.itemView.spotReservationBtn.isClickable = true
                    }
                }
            }
        }
        holder.bind(spot)
    }
}