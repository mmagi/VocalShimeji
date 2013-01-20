package com.group_finity.mascot.sound;

import com.sun.istack.internal.NotNull;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;

/**
 * Created with IntelliJ IDEA.
 * User: magi
 * Date: 12-12-26
 * Time: 下午1:47
 */
public final class SoundBuffer {
    public static final int FORMAT_MONO8 = 4352;
    public static final int FORMAT_MONO16 = 4353;
    public static final int FORMAT_STEREO8 = 4354;
    public static final int FORMAT_STEREO16 = 4355;
    final int bufferID;
    final ByteBuffer data;

    SoundBuffer(@NotNull AudioInputStream paramAudioInputStream) throws IOException {

        AudioFormat localAudioFormat = paramAudioInputStream.getFormat();

        if (localAudioFormat.getEncoding() != AudioFormat.Encoding.PCM_SIGNED) {
            localAudioFormat = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, localAudioFormat.getSampleRate(), 16, localAudioFormat.getChannels(), localAudioFormat.getChannels() * 2, localAudioFormat.getSampleRate(), false);
            paramAudioInputStream = AudioSystem.getAudioInputStream(localAudioFormat, paramAudioInputStream);
        }


        int channels = localAudioFormat.getChannels();
        int bitdepth = localAudioFormat.getSampleSizeInBits();
        if (bitdepth == AudioSystem.NOT_SPECIFIED)
            bitdepth = 16;
        int format = FORMAT_MONO8;

        if ((bitdepth == 8) && (channels == 1))
            format = FORMAT_MONO8;
        else if ((bitdepth == 16) && (channels == 1))
            format = FORMAT_MONO16;
        else if ((bitdepth == 8) && (channels == 2))
            format = FORMAT_STEREO8;
        else if ((bitdepth == 16) && (channels == 2)) {
            format = FORMAT_STEREO16;
        }
        int rate = Math.round(localAudioFormat.getSampleRate());
        final ByteBuffer localByteBuffer;
        if (paramAudioInputStream.getFrameLength() != AudioSystem.NOT_SPECIFIED) {
            int size = paramAudioInputStream.available();
            localByteBuffer = ByteBuffer.allocateDirect(size);
            while (localByteBuffer.remaining() > 0) {
                ReadableByteChannel localReadableByteChannel = Channels.newChannel(paramAudioInputStream);
                localReadableByteChannel.read(localByteBuffer);
            }
            localByteBuffer.rewind();
        } else {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            byte[] tmp = new byte[4096];
            int lenth;
            lenth = paramAudioInputStream.read(tmp);
            baos.write(tmp, 0, lenth);
            while (0 <= (lenth = paramAudioInputStream.read(tmp))) {
                baos.write(tmp, 0, lenth);
            }
            localByteBuffer = ByteBuffer.wrap(baos.toByteArray());
        }

        //        if (bitdepth == 16 &&  localAudioFormat.isBigEndian()) { //OpenAL要求必须little-endian
        //            int i1 = localByteBuffer.remaining();
        //            for (int i2 = 0; i2 < i1; i2 += 2) {
        //                byte b1 = localByteBuffer.get(i2);
        //                byte b2 = localByteBuffer.get(i2 + 1);
        //                localByteBuffer.put(i2, b2);
        //                localByteBuffer.put(i2 + 1, b1);
        //            }
        //        }
        data = localByteBuffer;

        int[] arrayOfInt = new int[1];
        SoundFactory.al.alGenBuffers(1, arrayOfInt, 0);
        this.bufferID = arrayOfInt[0];

        SoundFactory.al.alBufferData(this.bufferID, format, localByteBuffer, localByteBuffer.capacity(), rate);
    }

}
