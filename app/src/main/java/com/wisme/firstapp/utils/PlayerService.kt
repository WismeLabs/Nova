package com.wisme.firstapp.utils

import android.content.Intent
import android.media.AudioManager
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService

class PlayerService : MediaSessionService() {
    private var mediaSession: MediaSession? = null

    // The service is being created.
    override fun onCreate() {
        super.onCreate()
        
        println("PlayerService: *** CREATING PLAYER SERVICE ***")
        
        // Configure audio attributes for media playback with high volume
        val audioAttributes = AudioAttributes.Builder()
            .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC) // Changed to MUSIC for better volume
            .setUsage(C.USAGE_MEDIA)
            .setAllowedCapturePolicy(C.ALLOW_CAPTURE_BY_ALL)
            .build()
        
        val player = ExoPlayer.Builder(this)
            .setAudioAttributes(audioAttributes, true) // Handle audio focus automatically
            .setHandleAudioBecomingNoisy(true)
            .setWakeMode(C.WAKE_MODE_NETWORK) // Keep network awake for streaming
            .build()
        
        // Set higher volume
        player.volume = 1.0f // Maximum volume
        
        // Add comprehensive error listener
        player.addListener(object : Player.Listener {
            override fun onPlayerError(error: PlaybackException) {
                println("PlayerService: *** EXOPLAYER ERROR DETECTED ***")
                println("PlayerService: Error type: ${error.errorCode}")
                println("PlayerService: Error message: ${error.message}")
                
                when (error.errorCode) {
                    PlaybackException.ERROR_CODE_DECODING_FAILED -> {
                        println("PlayerService: *** AUDIO DECODING FAILED - CODEC ISSUE ***")
                        println("PlayerService: This device may not support the audio codec")
                        println("PlayerService: Cause: ${error.cause?.message}")
                    }
                    PlaybackException.ERROR_CODE_AUDIO_TRACK_INIT_FAILED -> {
                        println("PlayerService: *** AUDIO TRACK INITIALIZATION FAILED ***")
                        println("PlayerService: Audio output issue - check device audio settings")
                    }
                    PlaybackException.ERROR_CODE_AUDIO_TRACK_WRITE_FAILED -> {
                        println("PlayerService: *** AUDIO TRACK WRITE FAILED ***")
                        println("PlayerService: Audio playback interrupted or device audio issue")
                    }
                    else -> {
                        println("PlayerService: Other error: ${error.errorCodeName}")
                        println("PlayerService: Full error: $error")
                    }
                }
            }
            
            override fun onPlaybackStateChanged(playbackState: Int) {
                val stateString = when (playbackState) {
                    Player.STATE_IDLE -> "IDLE"
                    Player.STATE_BUFFERING -> "BUFFERING"
                    Player.STATE_READY -> "READY"
                    Player.STATE_ENDED -> "ENDED"
                    else -> "UNKNOWN"
                }
                println("PlayerService: Playback state changed to: $stateString")
            }
        })
        
        mediaSession = MediaSession.Builder(this, player).build()
        
        println("PlayerService: Created with audio attributes - Content: MUSIC, Usage: MEDIA, Volume: 1.0")
        println("PlayerService: Audio focus handling enabled, wake mode set for network")
        
        // Check system audio
        val audioManager = getSystemService(AUDIO_SERVICE) as AudioManager
        val maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
        val currentVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
        println("PlayerService: System media volume: $currentVolume/$maxVolume")
    }

    // The user dismissed the app from the recent tasks list.
    // Stop the service at this point.
    override fun onTaskRemoved(rootIntent: Intent?) {
        val player = mediaSession?.player!!
        if (!player.playWhenReady || player.mediaItemCount == 0) {
            stopSelf()
        }
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? {
        return mediaSession
    }

    // The service is being destroyed.
    override fun onDestroy() {
        mediaSession?.run {
            player.release()
            release()
            mediaSession = null
        }
        super.onDestroy()
    }
}
