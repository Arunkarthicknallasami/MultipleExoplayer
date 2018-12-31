package multi.exo.com.multi

import android.annotation.SuppressLint
import android.content.Context
import android.database.ContentObserver
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.util.Pair
import android.view.Gravity
import android.view.ViewGroup
import android.widget.FrameLayout
import com.google.android.exoplayer2.*
import com.google.android.exoplayer2.mediacodec.MediaCodecRenderer
import com.google.android.exoplayer2.mediacodec.MediaCodecUtil
import com.google.android.exoplayer2.source.ExtractorMediaSource
import com.google.android.exoplayer2.source.TrackGroupArray
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.trackselection.TrackSelectionArray
import com.google.android.exoplayer2.ui.TimeBar
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.util.ErrorMessageProvider
import com.google.android.exoplayer2.util.Util
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.exomediacontroller.*
import java.net.CookieHandler
import java.net.CookieManager
import java.net.CookiePolicy

class MainActivity : AppCompatActivity(), PlaybackPreparer {

    private var DEFAULT_COOKIE_MANAGER: CookieManager = CookieManager()

    var player: SimpleExoPlayer? = null
    var player2: SimpleExoPlayer? = null

    var videoUrl: String = ""
    var videoUrl2: String = ""
    private var mediaPlayer: MediaPlayer? = null


    init {
        DEFAULT_COOKIE_MANAGER.setCookiePolicy(CookiePolicy.ACCEPT_ORIGINAL_SERVER);
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        videoUrl = "http://commondatastorage.googleapis.com/gtv-videos-bucket/sample/WhatCarCanYouGetForAGrand.mp4"
        videoUrl2 = "http://www.obamadownloads.com/videos/mandela-day-speech.mp4`"
        initExoplayer(videoUrl)
        initExoplayer2(videoUrl2)
        initMusic()
    }

    private fun initMusic() {
        mediaPlayer = MediaPlayer()
        var attributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_MEDIA)
            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
            .build()
        mediaPlayer?.setAudioAttributes(attributes)
//        mediaPlayer?.setDataSource("http://www.largesound.com/ashborytour/sound/brobob.mp3")
        val afd = this@MainActivity.getAssets().openFd("sample.mp3")
        mediaPlayer?.setDataSource(afd.fileDescriptor, afd.startOffset, afd.length)
        afd.close()
        mediaPlayer?.isLooping = true
        mediaPlayer?.prepareAsync()
        mediaPlayer?.setOnPreparedListener { mp ->
            mp.start()
            val audio = this@MainActivity.getSystemService(Context.AUDIO_SERVICE) as AudioManager
            val currentVolume = audio.getStreamVolume(AudioManager.STREAM_MUSIC)
            val maxVolume = audio.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
            // var result = currentVolume.toFloat()/maxVolume*.50f
            var result = 0.10f
            mediaPlayer?.setVolume(result, result)
        }
        val mSettingsContentObserver = SettingsContentObserver(Handler())
        this.applicationContext.contentResolver.registerContentObserver(
            android.provider.Settings.System.CONTENT_URI, true,
            mSettingsContentObserver
        )
    }

    inner class SettingsContentObserver(handler: Handler) : ContentObserver(handler) {

        override fun deliverSelfNotifications(): Boolean {
            return super.deliverSelfNotifications()
        }

        override fun onChange(selfChange: Boolean) {
            super.onChange(selfChange)
            val audio = this@MainActivity.getSystemService(Context.AUDIO_SERVICE) as AudioManager
            val currentVolume = audio.getStreamVolume(AudioManager.STREAM_MUSIC)
            val maxVolume = audio.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
            if (currentVolume > (maxVolume/3)) {
                mediaPlayer?.setVolume(0.10f, 0.10f)
            }
        }
    }

    private fun initExoplayer(videoUrl: String) {
        if (CookieHandler.getDefault() !== DEFAULT_COOKIE_MANAGER) {
            CookieHandler.setDefault(DEFAULT_COOKIE_MANAGER)
        }
        val bandwidthMeter = DefaultBandwidthMeter()
        val videoTrackSelectionFactory = AdaptiveTrackSelection.Factory(bandwidthMeter)
        val trackSelector = DefaultTrackSelector(videoTrackSelectionFactory)

        player = ExoPlayerFactory.newSimpleInstance(this@MainActivity, trackSelector)
        player?.addListener(PlayerEventsListener())
        val dataSourceFactory = DefaultDataSourceFactory(
            this@MainActivity,
            Util.getUserAgent(this@MainActivity, "Multi"), bandwidthMeter
        )
        val videoSource = ExtractorMediaSource.Factory(dataSourceFactory)
            .createMediaSource(Uri.parse(videoUrl))
        player?.prepare(videoSource)
        playerView.apply {
            setErrorMessageProvider(PlayerErrorMessageProvider())
            requestFocus()
            setPlaybackPreparer(this@MainActivity)
        }
        playerView.player = player
        playerView.player.playWhenReady = true
        exo_progress.addListener(object : TimeBar.OnScrubListener {
            override fun onScrubMove(timeBar: TimeBar?, millis: Long) {
            }

            override fun onScrubStart(timeBar: TimeBar?, millis: Long) {
                player2?.playWhenReady = false
                player?.playWhenReady = false
            }

            override fun onScrubStop(timeBar: TimeBar?, millis: Long, canceled: Boolean) {
                var player2Mill = player2?.duration
                player2Mill?.let {
                    if (millis < player2Mill) {
                        player2?.seekTo(millis)
                        player2?.playWhenReady = true
                    } else {
                        player2?.seekTo(player2Mill)
                        player2?.playWhenReady = false
                    }
                }
                player?.playWhenReady = true
            }
        })
//        playerDummyView.setOnClickListener {
//            var width = playerDummyView.layoutParams.width
//            var height = playerDummyView.layoutParams.height
//            playerView2.layoutParams = topParam(width, height)
//            player2DummyView.layoutParams = topParam(width, height)
//            player2DummyView.visibility = View.VISIBLE
//            playerDummyView.visibility = View.GONE
//            playerView.layoutParams = fullScreenParam()
//            playerView.useController = true
//            playerView2.useController = false
//            player?.playWhenReady = false
//            player2?.playWhenReady = false
//            parentFrame.removeAllViews()
//            parentFrame.addView(playerView)
//            parentFrame.addView(playerView2)
//            parentFrame.addView(player2DummyView)
//            player?.playWhenReady = true
//            player2?.playWhenReady = true
//        }
        exo_play.setOnClickListener {
            player?.playWhenReady = true
            player2?.playWhenReady = true
        }
        exo_pause.setOnClickListener {
            player?.playWhenReady = false
            player2?.playWhenReady = false
        }
    }

    fun topParam(width: Int, height: Int): FrameLayout.LayoutParams {
        return FrameLayout.LayoutParams(
            width, height
        ).apply {
            gravity = Gravity.END
            setMargins(0, 20, 20, 0)
        }

    }

    fun fullScreenParam(): FrameLayout.LayoutParams {
        return FrameLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT
        ).apply {
            setMargins(0, 0, 0, 0)
        }
    }

    private fun initExoplayer2(videoUrl2: String) {
        if (CookieHandler.getDefault() !== DEFAULT_COOKIE_MANAGER) {
            CookieHandler.setDefault(DEFAULT_COOKIE_MANAGER)
        }
        val bandwidthMeter = DefaultBandwidthMeter()
        val videoTrackSelectionFactory = AdaptiveTrackSelection.Factory(bandwidthMeter)
        val trackSelector = DefaultTrackSelector(videoTrackSelectionFactory)

        player2 = ExoPlayerFactory.newSimpleInstance(this@MainActivity, trackSelector)
        player2?.addListener(PlayerEventsListener())
        val dataSourceFactory = DefaultDataSourceFactory(
            this@MainActivity,
            Util.getUserAgent(this@MainActivity, "Multi2"), bandwidthMeter
        )
        val videoSource = ExtractorMediaSource.Factory(dataSourceFactory)
            .createMediaSource(Uri.parse(videoUrl2))
        player2?.prepare(videoSource)
        playerView2.apply {
            setErrorMessageProvider(PlayerErrorMessageProvider())
            requestFocus()
            setPlaybackPreparer(this@MainActivity)
        }
        playerView2.player = player2
        playerView2.player.playWhenReady = true
//        player2DummyView.setOnClickListener {
//            var width = player2DummyView.layoutParams.width
//            var height = player2DummyView.layoutParams.height
//            playerView.layoutParams = topParam(width, height)
//            playerDummyView.layoutParams = topParam(width, height)
//            playerDummyView.visibility = View.VISIBLE
//            player2DummyView.visibility = View.GONE
//            playerView2.layoutParams = fullScreenParam()
//            playerView2.useController = true
//            playerView.useController = false
//            player?.playWhenReady = false
//            player2?.playWhenReady = false
//            parentFrame.removeAllViews()
//            parentFrame.addView(playerView2)
//            parentFrame.addView(playerView)
//            parentFrame.addView(playerDummyView)
//            player?.playWhenReady = true
//            player2?.playWhenReady = true
//
//        }
    }

    override fun onPause() {
        super.onPause()
        player?.apply {
            playWhenReady = false
        }
        player2?.apply {
            playWhenReady = false
        }
        mediaPlayer?.apply {
            if (isPlaying) {
                pause()
            }
        }

    }

    override fun onResume() {
        super.onResume()
        mediaPlayer?.apply {
            start()
        }
    }

    inner class PlayerEventsListener() : Player.EventListener {
        override fun onPlaybackParametersChanged(playbackParameters: PlaybackParameters?) {
        }

        override fun onSeekProcessed() {
        }

        override fun onTracksChanged(trackGroups: TrackGroupArray?, trackSelections: TrackSelectionArray?) {
        }

        override fun onPlayerError(error: ExoPlaybackException?) {
        }

        override fun onLoadingChanged(isLoading: Boolean) {
        }

        override fun onPositionDiscontinuity(reason: Int) {
        }

        override fun onRepeatModeChanged(repeatMode: Int) {
        }

        override fun onShuffleModeEnabledChanged(shuffleModeEnabled: Boolean) {
        }

        override fun onTimelineChanged(timeline: Timeline?, manifest: Any?, reason: Int) {
        }

        override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
            when (playbackState) {
                Player.STATE_ENDED -> {
                }
                Player.STATE_BUFFERING -> {
                }
                Player.STATE_READY -> {
                    //startTimer
                    Log.e("pos", "" + player?.currentPosition)
                }
            }
        }
    }

    private inner class PlayerErrorMessageProvider : ErrorMessageProvider<ExoPlaybackException> {
        @SuppressLint("StringFormatInvalid")
        override fun getErrorMessage(e: ExoPlaybackException): android.util.Pair<Int, String>? {
            var errorString = getString(R.string.error_generic)
            if (e.type == ExoPlaybackException.TYPE_RENDERER) {
                val cause = e.rendererException
                if (cause is MediaCodecRenderer.DecoderInitializationException) {
                    // Special case for decoder initialization failures.
                    errorString = if (cause.decoderName == null) {
                        when {
                            cause.cause is MediaCodecUtil.DecoderQueryException -> getString(R.string.error_querying_decoders)
                            cause.secureDecoderRequired -> getString(
                                R.string.error_no_secure_decoder, cause.mimeType
                            )
                            else -> getString(R.string.error_no_decoder, cause.mimeType)
                        }
                    } else {
                        getString(
                            R.string.error_instantiating_decoder,
                            cause.decoderName
                        )
                    }
                }
            }
            return Pair.create(0, errorString)
        }
    }

    override fun preparePlayback() {
    }
}
