package audio

import org.jtransforms.fft.DoubleFFT_1D
import java.io.File
import javax.sound.sampled.*


val retro = "data/music/Juhani Junkala [Retro Game Music Pack] Level 2.wav"
val arc = "data/music/Arc North - Daylight.wav"

fun main() {
    val w = waveform(2_000_000 / 3).drop(3000).take(1000).toDoubleArray()

    val spectrum = w.take(256).toDoubleArray()
    val fft = DoubleFFT_1D(spectrum.size.toLong())

    fft.realForward(spectrum)
//    array.toList().chunked(32) { chunk -> chunk.map { it.toInt() } }.forEach { println(it) }

    val bands = 16
    val l = IntArray(bands) { 0 }
    for (v in spectrum) {
        val i: Int = (v / (256 / bands)).toInt()
        if (i in 0 until bands) l[i]++
    }
}

// https://stackoverflow.com/questions/11017283/java-program-to-create-a-png-waveform-for-an-audio-file
fun waveform(w: Int = 400, h: Int = 30): DoubleArray {

    val file = File(arc)
    val stream: AudioInputStream = AudioSystem.getAudioInputStream(file)
    val format: AudioFormat = stream.format

    println("Format: $format")


    val bytes = ByteArray((stream.frameLength * format.frameSize).toInt())
    stream.read(bytes)

    val data = IntArray(bytes.size / 2)
    when (format.sampleSizeInBits) {
        16 -> read16bits(bytes, data, format.isBigEndian)
        8 -> TODO("8 bit not implemented")
        else -> TODO("errr..")
    }

    val framesPerPixel = bytes.size / format.frameSize / w
    val nChannels = format.channels

    val waveform = (0 until w).map { x ->
        val i = (framesPerPixel * nChannels * x)
        val value = if (format.sampleSizeInBits == 8) data[i] else 128 * data[i] / 32768
        val y = (h * (128 - value.toByte()) / 256).toDouble()
        y
    }

    return waveform.toDoubleArray()
}

fun printWaveform(waveform: DoubleArray, h: Int) {
    for (amp in waveform) {
        print(" ".repeat(h - amp.toInt()))
        print("–".repeat(amp.toInt() * 2))
        println()
    }
}

// fills audioData with values from audioBytes
fun read16bits(audioBytes: ByteArray, audioData: IntArray, isBigEndian: Boolean) {
    if (isBigEndian) {
        for (i in audioData.indices step 2) {
            val msb = audioBytes[i].toInt()
            val lsb = audioBytes[i + 1].toInt()
            audioData[i] = msb shl 8 or (255 and lsb)
        }
    } else {
        for (i in audioData.indices step 2) {
            val lsb = audioBytes[i].toInt()
            val msb = audioBytes[i + 1].toInt()
            audioData[i] = msb shl 8 or (255 and lsb)
        }
    }
}


fun playclip() {
    val rate = 50
    val clip = getClip()
    val audioFile = File("data/music/Juhani Junkala [Retro Game Music Pack] Level 2.wav")
    val audioStream = AudioSystem.getAudioInputStream(audioFile)

    clip.open(audioStream)

    val format = clip.format

    val decodedFormat = AudioFormat(
        AudioFormat.Encoding.PCM_SIGNED,
        format.sampleRate,
        16,
        format.channels,
        format.channels * 2,
        format.sampleRate,
        false
    )


    val decodedStream = AudioSystem.getAudioInputStream(decodedFormat, audioStream)
    clip.start()
    val inp = ByteArray(decodedFormat.sampleSizeInBits)
    var offset = 0
    do {
        val numBytesRead = decodedStream.read(inp, offset, decodedFormat.sampleSizeInBits)
        offset += numBytesRead
        println("${inp.toList()} ––– $numBytesRead")
        Thread.sleep(rate.toLong())
    } while (clip.isActive)
}

fun getClip(): Clip {
    val mixerInfos = AudioSystem.getMixerInfo()
//    for (info in mixerInfos) println("${info.name} ––– ${info.description}")
    val mixer = AudioSystem.getMixer(mixerInfos.first())

    val dataInfo = DataLine.Info(Clip::class.java, null)
    val clip = mixer.getLine(dataInfo) as Clip
    return clip
}


fun Clip.play() {
    start()
    do {
        Thread.sleep(50)
    } while (isActive)
}

fun stack() {
    val format = AudioFormat(8000.0f, 16, 1, true, true)
    val microphone: TargetDataLine
    try {
        val info = DataLine.Info(TargetDataLine::class.java, format)
        microphone = AudioSystem.getLine(info) as TargetDataLine
        microphone.open(format)
        microphone.start()

        val recording = object : Thread() {
            override fun run() {
                println("starting recording...")
                val stream = AudioInputStream(microphone)
                val file = File("test.wav")
                AudioSystem.write(stream, AudioFileFormat.Type.WAVE, file)
                println("recording stopped")

            }
        }

        recording.start()
        Thread.sleep(5000)
        microphone.stop()


        microphone.close()
    } catch (e: LineUnavailableException) {
        e.printStackTrace()
    }
}

fun test() {
    val mixerInfo = getBuiltInMicrophone()
    val builtInMicMixer = AudioSystem.getMixer(mixerInfo)
    val lineInfo = builtInMicMixer.targetLineInfo.first()


    builtInMicMixer.open()

    val line = AudioSystem.getLine(lineInfo) as TargetDataLine
    line.open()
    println(line.isOpen)


    val recording = object : Thread() {
        override fun run() {
            println("starting recording...")
            val stream = AudioInputStream(line)
            val file = File("test.wav")
            AudioSystem.write(stream, AudioFileFormat.Type.WAVE, file)
            println("recording stopped")

        }
    }

    line.start()
    recording.start()

    Thread.sleep(5000)

    line.stop()
    line.close()
}


fun getBuiltInMicrophone(): Mixer.Info {
    val target = "Built-in Microphone"
    //Enumerates all available microphones
    val mixerInfos = AudioSystem.getMixerInfo()
    for ((i, info) in mixerInfos.withIndex()) {
        val m = AudioSystem.getMixer(info)
        val lineInfos = m.targetLineInfo
        if (lineInfos.size >= 1 && lineInfos[0].lineClass.equals(TargetDataLine::class.java)) {
            //Only prints out info is it is a Microphone
            println("Line$i Name: " + info.name) //The name of the AudioDevice
            println("Line$i Description: " + info.description) //The type of audio device
            for (lineInfo in lineInfos) {
                println("\t---$lineInfo")
                val line: Line = try {
                    m.getLine(lineInfo)
                } catch (e: LineUnavailableException) {
                    throw e
                }
                println("\t-----$line")

                if (info.name == target) {
                    println(">> found $target <<")
                    return info
                }
            }
        }
    }
    error("failed to find $target")
}

fun docsOracle() {
    val format = AudioFormat(8000f, 16, 1, true, false)
    val line: TargetDataLine
    val info = DataLine.Info(TargetDataLine::class.java, format) // format is an AudioFormat object

    if (!AudioSystem.isLineSupported(info)) {
        error("$info not supported")
    }
    // Obtain and open the line.
    try {
        line = AudioSystem.getLine(info) as TargetDataLine
        line.open(format)

        line.start()

        val recording = object : Thread() {
            override fun run() {
                println("starting recording...")
                val stream = AudioInputStream(line)
                val file = File("test.wav")
                AudioSystem.write(stream, AudioFileFormat.Type.WAVE, file)
                println("recording stopped")

            }
        }

        recording.start()
        Thread.sleep(5000)
        line.stop()
        recording.interrupt()
        line.close()
    } catch (ex: LineUnavailableException) {
        error(ex)
    }
}




