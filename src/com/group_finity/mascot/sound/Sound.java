package com.group_finity.mascot.sound;

import com.jogamp.openal.sound3d.AudioSystem3D;
import com.jogamp.openal.sound3d.Buffer;
import com.sun.istack.internal.NotNull;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created with IntelliJ IDEA.
 * User: magi
 * Date: 12-12-26
 * Time: 下午1:47
 */
public final class Sound {
    public final Buffer buffer;

    Sound(@NotNull InputStream in) throws IOException, UnsupportedAudioFileException {
            buffer = AudioSystem3D.loadBuffer(in);
    }
}
