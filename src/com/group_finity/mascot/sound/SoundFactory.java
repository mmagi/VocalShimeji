package com.group_finity.mascot.sound;

import com.group_finity.mascot.Manager;
import com.group_finity.mascot.Mascot;
import com.jogamp.openal.AL;
import com.jogamp.openal.ALFactory;
import com.jogamp.openal.sound3d.AudioSystem3D;
import com.jogamp.openal.sound3d.Context;
import com.jogamp.openal.sound3d.Device;
import com.jogamp.openal.sound3d.Listener;

import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class SoundFactory {
    static final Logger log = Logger.getLogger(Manager.class.getName());
    private static final ConcurrentHashMap<String, SoundBuffer> soundCache = new ConcurrentHashMap<String, SoundBuffer>();
    public static boolean voiceOn = true;
    public static boolean sfxOn = true;
    public static final int defaultVoicePriority = -10;

    @SuppressWarnings("WeakerAccess")
    public static final int defaultSleepMSec = 100;

    static {
        AudioSystem3D.init();

        Device localDevice = AudioSystem3D.openDevice(null);
        Context localContext = AudioSystem3D.createContext(localDevice);
        AudioSystem3D.makeContextCurrent(localContext);

        Listener localListener = AudioSystem3D.getListener();
        localListener.setPosition(0.0F, 0.0F, 0.0F);
    }

    public static SoundBuffer getSound(String resPath) {
        if (null == resPath) return null;
        SoundBuffer sound = soundCache.get(resPath);
        if (null == sound) {
            try {
                sound = new SoundBuffer(AudioSystem.getAudioInputStream(SoundFactory.class.getResource("/media" + resPath)));
                soundCache.put(resPath, sound);
            } catch (UnsupportedAudioFileException e) {
                log.log(Level.WARNING, "音频文件{0}的格式不支持", resPath);
            } catch (Exception e) {
                log.log(Level.WARNING, "载入音频文件{0}时出错", resPath);
            }
        }
        return sound;
    }

    private static ActionWithSoundInvoker invoker = new ActionWithSoundInvoker();

    static {
        invoker.setName("ActionWithSoundInvoker");
        invoker.start();
    }

    public static void invokeAfterSound(final SoundBuffer sound, final Runnable cmd) {
        invoker.Invoke(sound, cmd);
    }

    public synchronized static VoiceController getVoiceController(Mascot mascot) {
        return new VoiceController() {
            final SoundSource localSource = new SoundSource();
            {
                localSource.setPosition(0.0F, 0.0F, 0.0F);
                localSource.setLooping(false);
            }
            volatile SoundBuffer lastPlayed;
            volatile int curLevel = 0;

            @Override
            public void speak(SoundBuffer voice, int pri) {
                if (voiceOn) {
                    int priority = Math.abs(pri);
                        if (localSource.isPlaying())
                            if (priority > curLevel || (priority == curLevel && pri < 0)) {
                                localSource.stop();
                            } else {
                                return;
                            }
                    curLevel = priority;
                    lastPlayed = voice;
                    localSource.setBuffer(voice);
                    localSource.play();
                }
            }

            @Override
            public void release() {
                if (null != localSource) localSource.delete();
            }

            @Override
            public SoundBuffer getLastPlayed() {
                return lastPlayed;
            }
        };
    }


    public synchronized static SfxController getSfxController(Mascot mascot) {
        return new SfxController() {
            SoundSource localSource = new SoundSource();
            SoundSource localSourceBack = new SoundSource();

            {
                localSource.setPosition(0.0F, 0.0F, 0.0F);
                localSource.setLooping(false);
                localSourceBack.setPosition(0.0F, 0.0F, 0.0F);
                localSourceBack.setLooping(false);
            }

            @Override
            public void sound(SoundBuffer sound) {
                if (sfxOn) {
                    localSource.stop();
                    localSource.setBuffer(sound);
                    localSource.play();
                    final SoundSource swap = localSource;
                    localSource = localSourceBack;
                    localSourceBack = swap;
                }
            }

            @Override
            public void release() {
                if (null != localSource) localSource.delete();
                if (null != localSourceBack) localSourceBack.delete();
            }
        };
    }

    static AL al = ALFactory.getAL();


}

