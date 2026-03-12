package com.noodrop.app.util

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool

/**
 * Lightweight SoundPool wrapper for satisfying UI sounds.
 * Called once in NoodropApp.onCreate().
 *
 * Sound files go in:  app/src/main/res/raw/
 *   sound_check.wav  – soft tick  (compound checked off)
 *   sound_log.wav    – short chime (day logged)
 *   sound_stack.wav  – pop/bubble (compound added to stack)
 *
 * If a file is missing the app won't crash — the sound is simply skipped.
 */
object SoundManager {

    private var pool: SoundPool? = null
    private var idCheck = 0
    private var idLog   = 0
    private var idStack = 0

    fun init(context: Context) {
        pool = SoundPool.Builder()
            .setMaxStreams(3)
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build()
            )
            .build()

        idCheck = loadRaw(context, "sound_check")
        idLog   = loadRaw(context, "sound_log")
        idStack = loadRaw(context, "sound_stack")
    }

    /** Soft tick — compound checked off in tracker */
    fun playCheck() = play(idCheck)

    /** Ascending chime — day logged */
    fun playLog()   = play(idLog)

    /** Pop — compound added to stack */
    fun playStack() = play(idStack)

    fun release() {
        pool?.release()
        pool = null
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private fun loadRaw(context: Context, name: String): Int {
        return try {
            val resId = context.resources.getIdentifier(name, "raw", context.packageName)
            if (resId != 0) pool?.load(context, resId, 1) ?: 0 else 0
        } catch (e: Exception) { 0 }
    }

    private fun play(id: Int) {
        if (id != 0) pool?.play(id, 0.9f, 0.9f, 1, 0, 1.0f)
    }
}
