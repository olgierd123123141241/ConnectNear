package pl.example.connectnear

import android.content.Context
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.net.Uri
import android.os.Build
import android.util.Log
import java.io.File
import java.io.FileOutputStream

class AudioRecorder(private val context: Context) {
    private var recorder: MediaRecorder? = null
    private var outputFile: File? = null
    private var startTime: Long = 0

    fun start(): Boolean {
        outputFile = File(context.cacheDir, "audio_record_${System.currentTimeMillis()}.3gp")
        recorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            MediaRecorder(context)
        } else {
            MediaRecorder()
        }.apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
            setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
            setOutputFile(outputFile!!.absolutePath)
            try {
                prepare()
                start()
                startTime = System.currentTimeMillis()
            } catch (e: Exception) {
                Log.e("AudioRecorder", "Start failed: ${e.message}")
                return false
            }
        }
        return true
    }

    fun stop(): Pair<File?, Long>? {
        if (recorder == null || outputFile == null) return null
        
        return try {
            recorder?.stop()
            recorder?.release()
            val duration = System.currentTimeMillis() - startTime
            recorder = null
            Pair(outputFile!!, duration)
        } catch (e: Exception) {
            Log.e("AudioRecorder", "Stop failed: ${e.message}")
            recorder = null
            null
        }
    }
}

class AudioPlayer(private val context: Context) {
    private var player: MediaPlayer? = null

    fun playFile(url: String) {
        stop()
        player = MediaPlayer().apply {
            try {
                setDataSource(url)
                prepare()
                start()
            } catch (e: Exception) {
                Log.e("AudioPlayer", "Play failed: ${e.message}")
            }
        }
    }

    fun stop() {
        player?.release()
        player = null
    }
}
