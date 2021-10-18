package info.meodinger.lpfx.util.media

import info.meodinger.lpfx.util.using

import javafx.scene.media.Media
import javafx.scene.media.MediaPlayer
import java.net.URL
import javax.sound.sampled.*


/**
 * Author: Meodinger
 * Date: 2021/10/15
 * Location: info.meodinger.lpfx.util
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
fun playOgg(mediaURL: URL, callback: () -> Unit = {}) {
    using {
        val rawStream = AudioSystem.getAudioInputStream(mediaURL)
        val ch: Int = rawStream.format.channels
        val rate: Float = rawStream.format.sampleRate
        val outFormat = AudioFormat(AudioFormat.Encoding.PCM_SIGNED, rate, 16, ch, ch * 2, rate, false)

        val clip = AudioSystem.getClip()
        clip.addLineListener { e -> if (e.type == LineEvent.Type.STOP) callback() }
        clip.open(AudioSystem.getAudioInputStream(outFormat, rawStream))
        clip.start()
    } catch { e: Throwable ->
        throw IllegalStateException(e)
    } finally { }
}

/**
 * Play ogg list
 */
fun playOggList(vararg mediaURLs: URL, callback: () -> Unit = {}) {
    val clipList = ArrayList<Clip>()

    fun play(index: Int) {
        if (index >= mediaURLs.size) callback()
        clipList[index].start()
    }

    for (i in mediaURLs.indices) {
        val raw = AudioSystem.getAudioInputStream(mediaURLs[i])
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