package com.rizek.tiebreaker.ui

import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.rizek.tiebreaker.R
import com.rizek.tiebreaker.databinding.ItemOptionRowBinding

/**
 * RecyclerView adapter for dynamic option input rows.
 * Supports adding and removing rows, with a minimum of 2.
 */
class OptionsAdapter(
    private val onRemove: (Int) -> Unit
) : RecyclerView.Adapter<OptionsAdapter.OptionViewHolder>() {

    private val options = mutableListOf("", "")  // Start with 2 empty rows

    val itemCount2: Int get() = options.size

    fun addOption(): Boolean {
        if (options.size >= 10) return false
        options.add("")
        notifyItemInserted(options.lastIndex)
        return true
    }

    fun removeOption(position: Int): Boolean {
        if (options.size <= 2 || position !in options.indices) return false
        options.removeAt(position)
        notifyItemRemoved(position)
        notifyItemRangeChanged(position, options.size - position)
        return true
    }

    fun getFilledOptions(): List<String> {
        return options.map { it.trim() }.filter { it.isNotEmpty() }
    }

    override fun getItemCount(): Int = options.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OptionViewHolder {
        val binding = ItemOptionRowBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return OptionViewHolder(binding)
    }

    override fun onBindViewHolder(holder: OptionViewHolder, position: Int) {
        holder.bind(position)
    }

    inner class OptionViewHolder(
        private val binding: ItemOptionRowBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        private var textWatcher: TextWatcher? = null

        fun bind(position: Int) {
            // Remove old watcher to prevent double-firing
            textWatcher?.let { binding.editOption.removeTextChangedListener(it) }

            binding.editOption.setText(options[position])
            binding.inputLayout.hint = binding.root.context.getString(
                R.string.hint_option, position + 1
            )

            // Show/hide remove button (min 2 rows)
            binding.btnRemove.visibility = if (options.size > 2) {
                android.view.View.VISIBLE
            } else {
                android.view.View.INVISIBLE
            }

            textWatcher = object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
                override fun afterTextChanged(s: Editable?) {
                    val pos = bindingAdapterPosition
                    if (pos != RecyclerView.NO_POSITION && pos < options.size) {
                        options[pos] = s.toString()
                    }
                }
            }
            binding.editOption.addTextChangedListener(textWatcher)

            binding.btnRemove.setOnClickListener {
                val pos = bindingAdapterPosition
                if (pos != RecyclerView.NO_POSITION) {
                    onRemove(pos)
                }
            }
        }
    }
}
