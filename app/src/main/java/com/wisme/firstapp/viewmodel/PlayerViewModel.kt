package com.wisme.firstapp.viewmodel

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import com.wisme.firstapp.R
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class PlayerViewModel(application: Application, private val mediaController: MediaController?) : AndroidViewModel(application) {

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying = _isPlaying.asStateFlow()

    private val _currentPosition = MutableStateFlow(0L)
    val currentPosition = _currentPosition.asStateFlow()

    private val _totalDuration = MutableStateFlow(0L)
    val totalDuration = _totalDuration.asStateFlow()

    private var positionJob: Job? = null

    private val playerListener = object : Player.Listener {
        override fun onIsPlayingChanged(isPlaying: Boolean) {
            _isPlaying.value = isPlaying
            if (isPlaying) {
                startPositionUpdates()
            } else {
                stopPositionUpdates()
            }
        }

        override fun onPlaybackStateChanged(playbackState: Int) {
            if (playbackState == Player.STATE_READY) {
                _totalDuration.value = mediaController?.duration ?: 0L
            }
        }
    }

    init {
        mediaController?.addListener(playerListener)

        val uri = Uri.parse("android.resource://${application.packageName}/${R.raw.podcast}")
        val mediaItem = MediaItem.fromUri(uri)
        mediaController?.setMediaItem(mediaItem)
        mediaController?.prepare()
    }

    fun playPause() {
        if (mediaController?.isPlaying == true) {
            mediaController.pause()
        } else {
            mediaController?.play()
        }
    }

    private fun startPositionUpdates() {
        stopPositionUpdates()
        positionJob = viewModelScope.launch {
            while (_isPlaying.value) {
                _currentPosition.value = mediaController?.currentPosition ?: 0L
                delay(1000) // Update every second
            }
        }
    }

    private fun stopPositionUpdates() {
        positionJob?.cancel()
        positionJob = null
    }

    fun seekForward() {
        mediaController?.let {
            val newPosition = (it.currentPosition + 10000).coerceAtMost(it.duration)
            it.seekTo(newPosition)
            _currentPosition.value = newPosition // Immediately update UI
        }
    }

    fun seekBack() {
        mediaController?.let {
            val newPosition = (it.currentPosition - 10000).coerceAtLeast(0)
            it.seekTo(newPosition)
            _currentPosition.value = newPosition // Immediately update UI
        }
    }

    fun seekTo(position: Float) {
        val newPosition = (position * (_totalDuration.value)).toLong()
        mediaController?.seekTo(newPosition)
        _currentPosition.value = newPosition // Immediately update UI for better UX
    }

    override fun onCleared() {
        super.onCleared()
        mediaController?.removeListener(playerListener)
        stopPositionUpdates()
        // The MediaController is released in the Activity/Fragment's onDispose
    }
}
