package engine;

import javazoom.jl.decoder.*;
import observers.Event;
import observers.EventSystem;
import observers.EventType;
import org.lwjgl.system.MemoryUtil;

import javax.sound.sampled.*;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;

import static org.lwjgl.openal.AL10.*;
import static org.lwjgl.stb.STBVorbis.stb_vorbis_decode_filename;
import static org.lwjgl.system.MemoryStack.stackPush;

public class Sound {

    private final int bufferId;
    private final int sourceId;
    private boolean isPlaying = false;
    private final String filepath;

    public Sound(String filepath, boolean loops) {
        this.filepath = filepath;

        AudioData data;

        String lower = filepath.toLowerCase();
        if (lower.endsWith(".ogg")) {
            data = decodeOgg(filepath);
        } else if (lower.endsWith(".mp3")) {
            data = decodeMp3(filepath);
        } else {
            data = decodeJava(filepath); // WAV, AIFF
        }

        bufferId = alGenBuffers();
        alBufferData(bufferId, data.format, data.buffer, data.sampleRate);

        sourceId = alGenSources();
        alSourcei(sourceId, AL_BUFFER, bufferId);
        alSourcei(sourceId, AL_LOOPING, loops ? AL_TRUE : AL_FALSE);
        alSourcef(sourceId, AL_GAIN, 0.3f);

        MemoryUtil.memFree(data.buffer);
    }

    // ======================================================
    // OGG – stb_vorbis
    // ======================================================
    private AudioData decodeOgg(String path) {
        try (var stack = stackPush()) {
            IntBuffer channelsBuf = stack.mallocInt(1);
            IntBuffer sampleRateBuf = stack.mallocInt(1);

            ShortBuffer raw = stb_vorbis_decode_filename(path, channelsBuf, sampleRateBuf);
            if (raw == null) {
                throw new RuntimeException("Failed to decode OGG: " + path);
            }

            int channels = channelsBuf.get(0);
            int sampleRate = sampleRateBuf.get(0);
            int format = channels == 1 ? AL_FORMAT_MONO16 : AL_FORMAT_STEREO16;

            ByteBuffer buffer = MemoryUtil.memAlloc(raw.remaining() * 2);
            for (int i = 0; i < raw.remaining(); i++) {
                buffer.putShort(raw.get(i));
            }
            buffer.flip();

            return new AudioData(buffer, format, sampleRate);
        }
    }


    // ======================================================
    // WAV / AIFF – Java AudioSystem
    // ======================================================
    private AudioData decodeJava(String path) {
        try {
            AudioInputStream ais = AudioSystem.getAudioInputStream(new File(path));
            AudioFormat base = ais.getFormat();

            AudioFormat decoded = new AudioFormat(
                    AudioFormat.Encoding.PCM_SIGNED,
                    base.getSampleRate(),
                    16,
                    base.getChannels(),
                    base.getChannels() * 2,
                    base.getSampleRate(),
                    false
            );

            AudioInputStream pcm = AudioSystem.getAudioInputStream(decoded, ais);
            byte[] bytes = pcm.readAllBytes();

            ByteBuffer buffer = MemoryUtil.memAlloc(bytes.length);
            buffer.put(bytes).flip();

            int format = decoded.getChannels() == 1 ? AL_FORMAT_MONO16 : AL_FORMAT_STEREO16;
            return new AudioData(buffer, format, (int) decoded.getSampleRate());

        } catch (Exception e) {
            Window.addError(e.getMessage());
            EventSystem.notify(null, new Event(EventType.ErrorEvent));
            assert false : e.getMessage();
        }
        return null;
    }

    // ======================================================
    // MP3 – JLayer
    // ======================================================
    private AudioData decodeMp3(String path) {
        try (FileInputStream fis = new FileInputStream(path)) {
            Bitstream bitstream = new Bitstream(fis);
            Decoder decoder = new Decoder();

            ByteArrayOutputStream baos = new ByteArrayOutputStream();

            int channels = 0;
            int sampleRate = 0;

            Header frame;
            while ((frame = bitstream.readFrame()) != null) {
                SampleBuffer output = (SampleBuffer) decoder.decodeFrame(frame, bitstream);
                short[] samples = output.getBuffer();

                if (channels == 0) channels = output.getChannelCount();
                if (sampleRate == 0) sampleRate = output.getSampleFrequency();

                for (short s : samples) {
                    baos.write(s & 0xff);
                    baos.write((s >> 8) & 0xff);
                }

                bitstream.closeFrame();
            }

            byte[] audioBytes = baos.toByteArray();
            ByteBuffer buffer = MemoryUtil.memAlloc(audioBytes.length);
            buffer.put(audioBytes).flip();

            int format = channels == 1 ? AL_FORMAT_MONO16 : AL_FORMAT_STEREO16;

            return new AudioData(buffer, format, sampleRate);

        } catch (Exception e) {
            Window.addError(e.getMessage());
            EventSystem.notify(null, new Event(EventType.ErrorEvent));
            assert false : e.getMessage();
        }
        return null;
    }

    // ======================================================
    // API
    // ======================================================
    public void playOnlyOne() {
        int state = alGetSourcei(sourceId, AL_SOURCE_STATE);
        if (state != AL_PLAYING) {
            alSourcePlay(sourceId);
            isPlaying = true;
        }
    }

    public void play() {
        alSourcePlay(sourceId);
        isPlaying = true;
    }


    public void stop() {
        alSourceStop(sourceId);
        isPlaying = false;
    }

    public boolean isPlaying() {
        isPlaying = alGetSourcei(sourceId, AL_SOURCE_STATE) == AL_PLAYING;
        return isPlaying;
    }


    public void delete() {
        alDeleteSources(sourceId);
        alDeleteBuffers(bufferId);
    }

    public void setVolume(float gain) {
        alSourcef(sourceId, AL_GAIN, gain);
    }

    // ======================================================
    // Helper class
    // ======================================================
    private static class AudioData {
        final ByteBuffer buffer;
        final int format;
        final int sampleRate;

        AudioData(ByteBuffer buffer, int format, int sampleRate) {
            this.buffer = buffer;
            this.format = format;
            this.sampleRate = sampleRate;
        }
    }
}
