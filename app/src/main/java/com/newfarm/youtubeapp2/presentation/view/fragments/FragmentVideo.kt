package com.newfarm.youtubeapp2.presentation.view.fragments

import android.os.Bundle
import com.google.android.youtube.player.YouTubeInitializationResult
import com.google.android.youtube.player.YouTubePlayer
import com.google.android.youtube.player.YouTubePlayerFragment
import com.newfarm.youtubeapp2.remote.common.YOUTUBE_APIKEY

class FragmentVideo : YouTubePlayerFragment(), YouTubePlayer.OnInitializedListener {

    private var player: YouTubePlayer? = null
    private var videoId: String? = null

    override fun onCreate(p0: Bundle?) {
        super.onCreate(p0)
        initialize(YOUTUBE_APIKEY, this)
    }

    override fun onDestroy() {
        if (player != null) {
            player!!.release()
        }
        super.onDestroy()
    }

    fun setVideoId(videoId: String?) {
        if (videoId != null && videoId != this.videoId) {
            this.videoId = videoId
            if (player != null) {
                player!!.cueVideo(videoId)
            }
        }
    }

    override fun onInitializationSuccess(provider: YouTubePlayer.Provider?, player: YouTubePlayer?, restored: Boolean) {

        this.player = player

        player?.addFullscreenControlFlag(YouTubePlayer.FULLSCREEN_FLAG_ALWAYS_FULLSCREEN_IN_LANDSCAPE)
        player?.setOnFullscreenListener(activity as YouTubePlayer.OnFullscreenListener)

        if (!restored && videoId != null) {
            player?.cueVideo(videoId)
        }
    }

    override fun onInitializationFailure(p0: YouTubePlayer.Provider?, p1: YouTubeInitializationResult?) {
        this.player = null
    }

    fun backnormal() {
        player?.setFullscreen(false)
    }
}