package com.project.himanshu.sltmuve.ui.adaptars

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.project.himanshu.sltmuve.data.dataModel.VehicleType
import com.project.himanshu.sltmuve.databinding.ListVehicleTypeBinding



class VehicleTypeAdaptar :
    ListAdapter<VehicleType, RecyclerView.ViewHolder>(CustomNewsDiffCallback()) {

    lateinit var mClickListener: ClickListener
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val vehicletype = getItem(position)
        (holder as VehicleTypeViewHolder).bind(vehicletype)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return VehicleTypeViewHolder(
            ListVehicleTypeBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            ),mClickListener
        )
    }
    fun setOnItemClickListener(aClickListener: ClickListener) {
        mClickListener = aClickListener
    }
    interface ClickListener {
        fun onClick(vehicletype: VehicleType, aView: View)
    }

    class VehicleTypeViewHolder(private val binding: ListVehicleTypeBinding,var mClickListener: ClickListener) :
        RecyclerView.ViewHolder(binding.root) {
        init {
            binding.setClickListener {
                binding.vehicletype?.let { type ->
                    mClickListener.onClick(type,it)
                }
            }
        }

        fun bind(type: VehicleType) {
            binding.apply {
                vehicletype = type
                executePendingBindings()
            }
        }
    }

}

 private class CustomNewsDiffCallback : DiffUtil.ItemCallback<VehicleType>() {
    override fun areItemsTheSame(oldItem: VehicleType, newItem: VehicleType): Boolean {
        return oldItem.typeID == newItem.typeID
    }

    override fun areContentsTheSame(oldItem: VehicleType, newItem: VehicleType): Boolean {
        return oldItem == newItem
    }
}
