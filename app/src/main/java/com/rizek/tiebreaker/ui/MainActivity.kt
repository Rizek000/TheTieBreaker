package com.rizek.tiebreaker.ui

import android.animation.ValueAnimator
import android.os.Bundle
import android.view.View
import android.view.animation.AnimationUtils
import android.view.animation.DecelerateInterpolator
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.rizek.tiebreaker.R
import com.rizek.tiebreaker.databinding.ActivityMainBinding
import com.rizek.tiebreaker.model.DecisionResult
import com.rizek.tiebreaker.util.HistoryManager
import com.rizek.tiebreaker.util.RouletteEngine

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var optionsAdapter: OptionsAdapter
    private lateinit var historyAdapter: HistoryAdapter
    private lateinit var historyManager: HistoryManager
    private lateinit var rouletteEngine: RouletteEngine

    private var historyExpanded = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        historyManager = HistoryManager(this)
        rouletteEngine = RouletteEngine(this)

        setupOptionsRecycler()
        setupHistoryRecycler()
        setupButtons()
    }

    override fun onDestroy() {
        super.onDestroy()
        rouletteEngine.cancel()
    }

    // ──────────────────────────────────────────────
    // Setup
    // ──────────────────────────────────────────────

    private fun setupOptionsRecycler() {
        optionsAdapter = OptionsAdapter { position ->
            optionsAdapter.removeOption(position)
        }
        binding.optionsRecycler.apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = optionsAdapter
            isNestedScrollingEnabled = false
        }
    }

    private fun setupHistoryRecycler() {
        historyAdapter = HistoryAdapter()
        binding.historyRecycler.apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = historyAdapter
            isNestedScrollingEnabled = false
        }
        refreshHistory()
    }

    private fun setupButtons() {
        binding.btnAddOption.setOnClickListener {
            if (!optionsAdapter.addOption()) {
                Toast.makeText(this, getString(R.string.error_max_options), Toast.LENGTH_SHORT).show()
            }
        }

        binding.btnDecide.setOnClickListener {
            onDecideClicked()
        }

        binding.historyHeader.setOnClickListener {
            toggleHistory()
        }
    }

    // ──────────────────────────────────────────────
    // Decide Logic
    // ──────────────────────────────────────────────

    private fun onDecideClicked() {
        if (rouletteEngine.spinning) return

        val options = optionsAdapter.getFilledOptions()
        if (options.size < 2) {
            Toast.makeText(this, getString(R.string.error_min_options), Toast.LENGTH_SHORT).show()
            return
        }

        // Disable button during spin
        binding.btnDecide.isEnabled = false
        binding.btnDecide.alpha = 0.5f

        // Show result card in spinning state
        binding.resultCard.visibility = View.VISIBLE
        binding.trophyIcon.visibility = View.GONE
        binding.winnerLabel.visibility = View.GONE
        binding.rouletteText.setTextColor(getColor(R.color.amber_accent))

        rouletteEngine.spin(options, object : RouletteEngine.Listener {
            override fun onTick(text: String) {
                binding.rouletteText.text = text
                binding.rouletteText.scaleX = 1.15f
                binding.rouletteText.scaleY = 1.15f
                binding.rouletteText.animate()
                    .scaleX(1.0f)
                    .scaleY(1.0f)
                    .setDuration(80)
                    .start()
            }

            override fun onWinner(winner: String) {
                showWinner(winner, options)
            }
        })
    }

    private fun showWinner(winner: String, options: List<String>) {
        // Update result display
        binding.rouletteText.text = winner
        binding.rouletteText.setTextColor(getColor(R.color.winner_green))
        binding.trophyIcon.visibility = View.VISIBLE
        binding.winnerLabel.visibility = View.VISIBLE

        // Play pulse animation
        val pulseAnim = AnimationUtils.loadAnimation(this, R.anim.pulse)
        binding.resultCard.startAnimation(pulseAnim)

        // Restore button
        val fadeBack = ValueAnimator.ofFloat(0.5f, 1.0f).apply {
            duration = 300
            interpolator = DecelerateInterpolator()
            addUpdateListener { binding.btnDecide.alpha = it.animatedValue as Float }
        }
        fadeBack.start()
        binding.btnDecide.isEnabled = true

        // Save to history & refresh
        val result = DecisionResult(winner = winner, options = options)
        historyManager.save(result)
        refreshHistory()
    }

    // ──────────────────────────────────────────────
    // History
    // ──────────────────────────────────────────────

    private fun toggleHistory() {
        historyExpanded = !historyExpanded

        // Rotate chevron
        val targetRotation = if (historyExpanded) 180f else 0f
        binding.historyChevron.animate()
            .rotation(targetRotation)
            .setDuration(200)
            .start()

        if (historyExpanded) {
            val history = historyManager.loadAll()
            if (history.isEmpty()) {
                binding.historyRecycler.visibility = View.GONE
                binding.historyEmpty.visibility = View.VISIBLE
            } else {
                historyAdapter.submitList(history)
                binding.historyRecycler.visibility = View.VISIBLE
                binding.historyEmpty.visibility = View.GONE
            }
        } else {
            binding.historyRecycler.visibility = View.GONE
            binding.historyEmpty.visibility = View.GONE
        }
    }

    private fun refreshHistory() {
        if (historyExpanded) {
            val history = historyManager.loadAll()
            if (history.isEmpty()) {
                binding.historyRecycler.visibility = View.GONE
                binding.historyEmpty.visibility = View.VISIBLE
            } else {
                historyAdapter.submitList(history)
                binding.historyRecycler.visibility = View.VISIBLE
                binding.historyEmpty.visibility = View.GONE
            }
        }
    }
}
