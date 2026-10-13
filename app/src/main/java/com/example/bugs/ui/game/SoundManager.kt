package com.example.bugs.ui.game

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.SoundPool
import com.example.bugs.R

class SoundManager(private val context: Context) {

    private var movePlayer: MediaPlayer? = null

    private var soundPool: SoundPool? = null
    private var killSoundId = 0
    private var missSoundId = 0
    private var bonusSoundId = 0

    fun init() {
        movePlayer = null
        soundPool = null
    }

    fun startMoveSound() {
        movePlayer?.let { player ->
            if (!player.isPlaying) {
                player.isLooping = true
                player.start()
            }
        }
    }

    fun stopMoveSound() {
        movePlayer?.let { player ->
            if (player.isPlaying) {
                player.pause()
                player.seekTo(0)
            }
        }
    }

    fun playKill() {
        soundPool?.play(killSoundId, 1f, 1f, 1, 0, 1f)
    }

    fun playMiss() {
        soundPool?.play(missSoundId, 1f, 1f, 1, 0, 1f)
    }

    fun playBonus() {
        soundPool?.play(bonusSoundId, 1f, 1f, 1, 0, 1f)
    }

    fun stopAll() {
        stopMoveSound()
        soundPool?.autoPause()
    }

    fun release() {
        movePlayer?.let {
            if (it.isPlaying) it.stop()
            it.release()
        }
        movePlayer = null

        soundPool?.release()
        soundPool = null
    }
}