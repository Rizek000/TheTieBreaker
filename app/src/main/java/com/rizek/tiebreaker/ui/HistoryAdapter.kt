package com.rizek.tiebreaker.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.rizek.tiebreaker.R
import com.rizek.tiebreaker.databinding.ItemHistoryBinding
import com.rizek.tiebreaker.model.DecisionResult
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * RecyclerView adapter for decision history entries.
 */
class HistoryAdapter : RecyclerView.Adapter<HistoryAdapter.HistoryViewHolder>() {

    private val items = mutableListOf<DecisionResult>()

    fun submitList(newItems: List<DecisionResult>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int = items.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoryViewHolder {
        val binding = ItemHistoryBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return HistoryViewHolder(binding)
    }

    override fun onBindViewHolder(holder: HistoryViewHolder, position: Int) {
        holder.bind(items[position])
    }

    inner class HistoryViewHolder(
        private val binding: ItemHistoryBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        private val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
        private val dateFormat = SimpleDateFormat("MMM d", Locale.getDefault())

        fun bind(result: DecisionResult) {
            binding.historyWinner.text = result.winner
            binding.historyMeta.text = binding.root.context.getString(
                R.string.options_summary, result.options.size
            )

            val date = Date(result.timestamp)
            val now = Date()
            binding.historyTime.text = if (isSameDay(date, now)) {
                timeFormat.format(date)
            } else {
                dateFormat.format(date)
            }
        }

        private fun isSameDay(d1: Date, d2: Date): Boolean {
            val fmt = SimpleDateFormat("yyyyMMdd", Locale.getDefault())
            return fmt.format(d1) == fmt.format(d2)
        }
    }
}
