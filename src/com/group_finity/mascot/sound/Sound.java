package com.group_finity.mascot.sound;

import com.sun.istack.internal.NotNull;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * Created with IntelliJ IDEA.
 * User: magi
 * Date: 12-12-26
 * Time: 下午1:47
 */
public final class Sound {
    final byte[] bytes;

    Sound(@NotNull AudioInputStream in) throws IOException {
        AudioFormat baseFormat = in.getFormat();
        if (!baseFormat.matches(SoundFactory.appAudioFormat)) {
            AudioFormat decodedFormat = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, baseFormat.getSampleRate(), 16, baseFormat.getChannels(), baseFormat.getChannels() * 2, baseFormat.getSampleRate(), false);
            in = AudioSystem.getAudioInputStream(decodedFormat, in);
            if (!decodedFormat.matches(SoundFactory.appAudioFormat)) {
                in = AudioSystem.getAudioInputStream(SoundFactory.appAudioFormat, in);
            }
        }
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        final byte[] temp = new byte[1024];
        int size = 0;
        while ((size = in.read(temp)) != -1) {
            out.write(temp, 0, size);
        }
        bytes = out.toByteArray();
        try {
            in.close();
        } catch (IOException e) {
            e.printStackTrace();//not care
        }
    }
}
