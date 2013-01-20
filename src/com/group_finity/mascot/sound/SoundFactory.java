package com.group_finity.mascot.sound;

import com.group_finity.mascot.Manager;
import com.group_finity.mascot.Mascot;
import com.group_finity.mascot.environment.Area;
import com.jogamp.openal.AL;
import com.jogamp.openal.ALFactory;
import com.jogamp.openal.sound3d.AudioSystem3D;
import com.jogamp.openal.sound3d.Context;
import com.jogamp.openal.sound3d.Device;
import com.jogamp.openal.sound3d.Listener;

import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.awt.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class SoundFactory {
    static final Logger log = Logger.getLogger(Manager.class.getName());
    private static final ConcurrentHashMap<String, SoundBuffer> soundCache = new ConcurrentHashMap<String, SoundBuffer>();
    public static boolean voiceOn = true;
    public static boolean sfxOn = true;
    public static boolean sound3D = true;
    public static final int defaultVoicePriority = -10;
    private static float screenZ = 0.3F;
    @SuppressWarnings("WeakerAccess")
    public static final int defaultSleepMSec = 100;

    static {
        AudioSystem3D.init();

        Device localDevice = AudioSystem3D.openDevice(null);
        Context localContext = AudioSystem3D.createContext(localDevice);
        AudioSystem3D.makeContextCurrent(localContext);

        Listener localListener = AudioSystem3D.getListener();

        localListener.setPosition(0F, 0F, 0.0F);
    }

    public static SoundBuffer getSound(String resPath) {
        if (null == resPath)
            return null;
        SoundBuffer sound = soundCache.get(resPath);
        if (null == sound) {
            try {
                sound = new SoundBuffer(AudioSystem.getAudioInputStream(SoundFactory.class.getResource("/media/" + resPath)));
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
                if (voiceOn && lastPlayed != voice) {
                    int priority = Math.abs(pri);
                    if (curLevel >= 0 && localSource.isPlaying())
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
            public void updatePosition(final Point anchor, final Area area) {
                if (voiceOn) {
                    if (curLevel >= 0) {
                        if (localSource.isPlaying()) {
                            final float x = anchor.x / (float) area.getWidth() - 0.5F;
                            final float y = anchor.y / (float) area.getHeight() - 0.5F;
                            localSource.setPosition(x, y, screenZ); //openal使用右手坐标系
                        } else {
                            curLevel = -1;
                        }
                    }
                } else {
                    if (curLevel >= 0) {
                        localSource.stop();
                        curLevel = -1;
                    }
                }
            }


            @Override
            public void release() {
                if (null != localSource)
                    localSource.delete();
            }

        };
    }


    public synchronized static SfxController getSfxController(Mascot mascot) {
        return new SfxController() {
            final SoundSource localSource = new SoundSource();

            {
                localSource.setPosition(0.0F, 0.0F, 0.0F);
                localSource.setLooping(true);
            }

            SoundBuffer buffer;

            @Override
            public void sound(final SoundBuffer sound) {
                if (sound != buffer) {
                    localSource.stop();
                    buffer = sound;
                    if (null != sound) {
                        localSource.setBuffer(sound);
                        localSource.play();
                    }
                }
            }


            public void updatePosition(final Point anchor, final Area area) {
                if (buffer != null) {
                    if (sfxOn) {
                        final float x = anchor.x / (float) area.getWidth() - 0.5F;
                        final float y = anchor.y / (float) area.getHeight() - 0.5F;
                        localSource.setPosition(x, y, screenZ);//右手坐标系
                    } else {
                        localSource.stop();
                        buffer = null;
                    }
                }
            }

            @Override
            public void release() {
                if (null != localSource)
                    localSource.delete();
            }
        };
    }

    static AL al = ALFactory.getAL();


}

