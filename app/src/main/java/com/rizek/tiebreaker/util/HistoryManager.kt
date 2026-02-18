package com.rizek.tiebreaker.util

import android.content.Context
import com.rizek.tiebreaker.model.DecisionResult
import org.json.JSONArray
import org.json.JSONObject

/**
 * Persists decision history in SharedPreferences (last 20 results).
 */
class HistoryManager(context: Context) {

    companion object {
        private const val PREFS_NAME = "tiebreaker_history"
        private const val KEY_HISTORY = "decisions"
        private const val MAX_HISTORY = 20
    }

    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun save(result: DecisionResult) {
        val history = loadAll().toMutableList()
        history.add(0, result)
        if (history.size > MAX_HISTORY) {
            history.removeAt(history.lastIndex)
        }
        val jsonArray = JSONArray()
        history.forEach { item ->
            val obj = JSONObject().apply {
                put("winner", item.winner)
                put("options", JSONArray(item.options))
                put("timestamp", item.timestamp)
            }
            jsonArray.put(obj)
        }
        prefs.edit().putString(KEY_HISTORY, jsonArray.toString()).apply()
    }

    fun loadAll(): List<DecisionResult> {
        val raw = prefs.getString(KEY_HISTORY, null) ?: return emptyList()
        return try {
            val jsonArray = JSONArray(raw)
            (0 until jsonArray.length()).map { i ->
                val obj = jsonArray.getJSONObject(i)
                val optionsArray = obj.getJSONArray("options")
                val options = (0 until optionsArray.length()).map { j ->
                    optionsArray.getString(j)
                }
                DecisionResult(
                    winner = obj.getString("winner"),
                    options = options,
                    timestamp = obj.getLong("timestamp")
                )
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun clear() {
        prefs.edit().remove(KEY_HISTORY).apply()
    }
}
