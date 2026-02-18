package com.rizek.tiebreaker.util

import android.content.Context
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager

/**
 * Encapsulates the roulette spin animation logic with deceleration and haptic feedback.
 */
class RouletteEngine(private val context: Context) {

    interface Listener {
        /** Called on each tick with the option text to display. */
        fun onTick(text: String)

        /** Called when the spin finishes with the winning option. */
        fun onWinner(winner: String)
    }

    companion object {
        private const val TOTAL_CYCLES = 30
        private const val MIN_DELAY_MS = 40L
        private const val MAX_DELAY_MS = 440L
        private const val TICK_VIBRATE_MS = 10L
        private const val WINNER_VIBRATE_MS = 80L
    }

    private val handler = Handler(Looper.getMainLooper())
    private var isRunning = false

    val spinning: Boolean get() = isRunning

    fun spin(options: List<String>, listener: Listener) {
        if (isRunning || options.size < 2) return
        isRunning = true

        val winnerIndex = options.indices.random()
        var currentCycle = 0

        fun scheduleNext() {
            if (currentCycle >= TOTAL_CYCLES) {
                listener.onTick(options[winnerIndex])
                vibrateStrong()
                listener.onWinner(options[winnerIndex])
                isRunning = false
                return
            }

            val progress = currentCycle.toFloat() / TOTAL_CYCLES
            val delay = (MIN_DELAY_MS + (MAX_DELAY_MS - MIN_DELAY_MS) * progress * progress).toLong()

            handler.postDelayed({
                val displayIndex = if (currentCycle >= TOTAL_CYCLES - 1) {
                    winnerIndex
                } else {
                    currentCycle % options.size
                }

                listener.onTick(options[displayIndex])
                vibrateLight()

                currentCycle++
                scheduleNext()
            }, delay)
        }

        scheduleNext()
    }

    fun cancel() {
        handler.removeCallbacksAndMessages(null)
        isRunning = false
    }

    private fun vibrateLight() {
        vibrate(TICK_VIBRATE_MS)
    }

    private fun vibrateStrong() {
        vibrate(WINNER_VIBRATE_MS)
    }

    @Suppress("DEPRECATION")
    private fun vibrate(durationMs: Long) {
        try {
            val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val mgr = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
                mgr.defaultVibrator
            } else {
                context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(
                    VibrationEffect.createOneShot(durationMs, VibrationEffect.DEFAULT_AMPLITUDE)
                )
            } else {
                vibrator.vibrate(durationMs)
            }
        } catch (_: Exception) {
            // Vibration not available — silently ignore
        }
    }
}
