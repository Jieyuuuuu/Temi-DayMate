package com.example.myapplication

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.media.AudioManager
import kotlin.concurrent.thread
import kotlin.math.PI
import kotlin.math.sin

object SoundUtil {
    fun playBeep(
        frequencyHz: Int = 880,
        durationMs: Int = 700,
        volume: Float = 0.8f
    ) {
        try {
            val sampleRate = 44100
            val numSamples = (durationMs * sampleRate) / 1000
            val buffer = ShortArray(numSamples)
            val amplitude = (volume.coerceIn(0f, 1f) * Short.MAX_VALUE).toInt()
            for (i in 0 until numSamples) {
                val angle = 2.0 * PI * i * frequencyHz / sampleRate
                buffer[i] = (amplitude * sin(angle)).toInt().toShort()
            }

            val attributes = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()

            val format = AudioFormat.Builder()
                .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                .setSampleRate(sampleRate)
                .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                .build()

            val track = AudioTrack(
                attributes,
                format,
                buffer.size * 2,
                AudioTrack.MODE_STATIC,
                AudioManager.AUDIO_SESSION_ID_GENERATE
            )
            track.write(buffer, 0, buffer.size)
            track.play()

            thread(start = true) {
                try {
                    Thread.sleep((durationMs + 100).toLong())
                } catch (_: Throwable) { }
                try {
                    track.stop()
                } catch (_: Throwable) { }
                try {
                    track.release()
                } catch (_: Throwable) { }
            }
        } catch (_: Throwable) {
        }
    }
}


