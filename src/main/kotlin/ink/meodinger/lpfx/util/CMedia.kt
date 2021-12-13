package ink.meodinger.lpfx.util.media

import javafx.scene.media.Media
import javafx.scene.media.MediaPlayer
import java.io.InputStream
import java.net.URL
import javax.sound.sampled.AudioFormat
import javax.sound.sampled.AudioSystem
import javax.sound.sampled.Clip
import javax.sound.sampled.LineEvent


/**
 * Author: Meodinger
 * Date: 2021/10/15
 * Have fun with my code!
 */

/**
 * Play media tracks
 */
fun playMediaTracks(mediaList: List<Media>, callback: () -> Unit = {}) {
    val players = ArrayList<MediaPlayer>()

    fun play(index: Int) {
        if (index >= players.size) callback()
        players[index].play()
    }

    for (i in mediaList.indices)
        players.add(MediaPlayer(mediaList[i]).also {
            it.setOnEndOfMedia { play(i + 1) }
        })

    play(0)
}

/**
 * Play ogg media
 *
 * Play music list please use playOggList
 */
fun playOgg(mediaStream: InputStream, callback: () -> Unit = {}) {
    val rawStream = AudioSystem.getAudioInputStream(mediaStream)

    val ch: Int = rawStream.format.channels
    val rate: Float = rawStream.format.sampleRate
    val outFormat = AudioFormat(AudioFormat.Encoding.PCM_SIGNED, rate, 16, ch, ch * 2, rate, false)

    val clip = AudioSystem.getClip()
    clip.addLineListener { e -> if (e.type == LineEvent.Type.STOP) callback() }
    clip.open(AudioSystem.getAudioInputStream(outFormat, rawStream))
    clip.start()
}
fun playOgg(mediaURL: URL, callback: () -> Unit = {}) = playOgg(mediaURL.openStream(), callback)

/**
 * Play ogg list
 */
fun playOggList(mediaStreamList: List<InputStream>, callback: () -> Unit = {}) {
    val clipList = ArrayList<Clip>()

    fun play(index: Int) {
        if (index >= mediaStreamList.size) callback()
        clipList[index].start()
    }

    for (i in mediaStreamList.indices) {
        val raw = AudioSystem.getAudioInputStream(mediaStreamList[i])
        val ch: Int = raw.format.channels
        val rate: Float = raw.format.sampleRate
        val format = AudioFormat(AudioFormat.Encoding.PCM_SIGNED, rate, 16, ch, ch * 2, rate, false)

        val clip = AudioSystem.getClip()
        clip.addLineListener { e -> if (e.type == LineEvent.Type.STOP) play(i + 1) }
        clip.open(AudioSystem.getAudioInputStream(format, raw))

        clipList.add(clip)
    }

    play(0)
}
fun playOggList(vararg mediaStreams: InputStream, callback: () -> Unit = {}) = playOggList(listOf(*mediaStreams), callback)
fun playOggList(vararg mediaURLs: URL, callback: () -> Unit = {}) = playOggList(List(mediaURLs.size) { mediaURLs[it].openStream() }, callback)
