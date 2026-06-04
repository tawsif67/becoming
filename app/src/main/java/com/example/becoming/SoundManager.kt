package com.example.becoming

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.SoundPool

class SoundManager(private val context: Context) {
    private var soundPool: SoundPool
    private var clickSoundId: Int = 0
    private var bgmPlayer: MediaPlayer? = null

    init {
        val audioAttributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_GAME)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()
        
        soundPool = SoundPool.Builder()
            .setMaxStreams(5)
            .setAudioAttributes(audioAttributes)
            .build()
        
        clickSoundId = soundPool.load(context, R.raw.click, 1)
    }

    fun playClick() {
        if (clickSoundId != 0) {
            soundPool.play(clickSoundId, 1f, 1f, 1, 0, 1f)
        }
    }

    fun startBgm() {
        if (bgmPlayer == null) {
            bgmPlayer = MediaPlayer.create(context, R.raw.bgm).apply {
                isLooping = true
                setVolume(0.5f, 0.5f)
                start()
            }
        } else if (!bgmPlayer!!.isPlaying) {
            bgmPlayer?.start()
        }
    }

    fun pauseBgm() {
        bgmPlayer?.pause()
    }

    fun release() {
        soundPool.release()
        bgmPlayer?.stop()
        bgmPlayer?.release()
        bgmPlayer = null
    }
}
