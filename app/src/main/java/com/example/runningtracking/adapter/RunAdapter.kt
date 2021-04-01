package com.example.runningtracking.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.runningtracking.R
import com.example.runningtracking.db.Run
import com.example.runningtracking.other.TrackingUtility
import java.text.SimpleDateFormat
import java.util.*

class RunAdapter : RecyclerView.Adapter<RunAdapter.RunViewHolder>() {


    /* Differ Callback Init*/
    private val differCallback = object : DiffUtil.ItemCallback<Run>() {
        override fun areItemsTheSame(oldItem: Run, newItem: Run): Boolean {
            return oldItem.id == newItem.id
        }
        override fun areContentsTheSame(oldItem: Run, newItem: Run): Boolean {
            return oldItem == newItem
        }


    }

    val differ = AsyncListDiffer(this, differCallback)

    fun submitList(list : List<Run>) = differ.submitList(list)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RunViewHolder {
        return RunViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.run_item,
                parent,
                false
            )
        )
    }


    override fun onBindViewHolder(holder: RunViewHolder, position: Int) {
        val run = differ.currentList[position]
        holder.itemView.apply {
            Glide.with(this).load(run.img).into(holder.ivMap)

            // Format Time
            val calendar = Calendar.getInstance().apply {
                timeInMillis = run.timeStamp
            }
            val dateFormat = SimpleDateFormat("dd.MM.yy", Locale.getDefault())
//            holder.tvDate.text = dateFormat.format(calendar)

            holder.tvSpeed.text = "${run.avgSpeedInKMH} km/h"

            holder.tvDistance.text = "${run.distanceInMeters / 1000f} km"

            holder.tvTime.text = TrackingUtility.getFormattedStopwatch(run.timeInMS)

            holder.tvCalories.text = "${run.caloriesBurned} kcal"


        }

    }

    override fun getItemCount(): Int = differ.currentList.size


    class RunViewHolder(itemView : View) : RecyclerView.ViewHolder(itemView){
        val tvDate : TextView
        val tvTime : TextView
        val tvDistance : TextView
        val tvSpeed : TextView
        val tvCalories : TextView
        val ivMap : ImageView

        init {
            tvDate = itemView.findViewById(R.id.tvDate)
            tvTime = itemView.findViewById(R.id.tvTime)
            tvDistance = itemView.findViewById(R.id.tvDistance)
            tvSpeed = itemView.findViewById(R.id.tvAvgSpeed)
            tvCalories = itemView.findViewById(R.id.tvCalories)
            ivMap = itemView.findViewById(R.id.ivRunImage)
        }
    }
}