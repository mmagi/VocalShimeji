package com.group_finity.mascot.sound;

import com.group_finity.mascot.Manager;
import com.group_finity.mascot.Mascot;
import com.jogamp.openal.AL;
import com.jogamp.openal.ALFactory;
import com.jogamp.openal.sound3d.AudioSystem3D;
import com.jogamp.openal.sound3d.Buffer;
import com.jogamp.openal.sound3d.Context;
import com.jogamp.openal.sound3d.Device;
import com.jogamp.openal.sound3d.Listener;
import com.jogamp.openal.sound3d.Source;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.util.Arrays;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class SoundFactory {
    static final Logger log = Logger.getLogger(Manager.class.getName());
    private static final ConcurrentHashMap<String, Sound> soundCache = new ConcurrentHashMap<String, Sound>();
    public static final AudioFormat appAudioFormat = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, 44100, 16, 2, 4, 44100, false); //标准CD音质
    public static boolean voiceOn = true;
    public static boolean sfxOn = true;
    public static final int defaultVoicePriority = -10;

    //CommonParams For Deamon
    @SuppressWarnings("WeakerAccess")
    public static final int defaultBufferSizeInMSec = 500;
    public static final int defaultBufferSize = SoundFactory.appAudioFormat.getFrameSize() * (int) (SoundFactory.appAudioFormat.getFrameRate() * defaultBufferSizeInMSec / 1000);
    public static final int defaultSleepMSec = 100;
    public static final int defaultWriteThreshold = SoundFactory.appAudioFormat.getFrameSize() * (int) (SoundFactory.appAudioFormat.getFrameRate() * defaultSleepMSec / 1200);
    public static final byte [] silence = new byte[defaultWriteThreshold];
    static {
        AudioSystem3D.init();

        Device localDevice = AudioSystem3D.openDevice(null);
        Context localContext = AudioSystem3D.createContext(localDevice);
        AudioSystem3D.makeContextCurrent(localContext);

        Listener localListener = AudioSystem3D.getListener();
        localListener.setPosition(0.0F, 0.0F, 0.0F);
    }

    public static Sound getSound(String resPath) {
        if (null == resPath) return null;
        Sound sound = soundCache.get(resPath);
        if (null == sound) {
            try {
                sound = new Sound(SoundFactory.class.getResourceAsStream("/media" + resPath));
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
    public static void invokeAfterSound(final Sound sound,final Runnable cmd) {
        invoker.Invoke(sound, cmd);
    }

    public synchronized static VoiceController getVoiceController(Mascot mascot) {
        return new VoiceController(){
            Source localSource;
            volatile Sound lastPlayed;
            volatile int curLevel = 0;
            @Override
            public void speak(Sound voice, int pri) {
                if(null == localSource){
                    localSource = AudioSystem3D.generateSource(voice.buffer);
                    localSource.setPosition(0.0F, 0.0F, 0.0F);
                    localSource.setLooping(false);
                }
                int priority = Math.abs(pri);
                if (0 == localSource.getBuffersProcessed() )
                    if (priority > curLevel || (priority == curLevel && pri < 0)) {
                        localSource.stop();
                    }else{
                        return;
                    }
                curLevel = priority;
                lastPlayed = voice;
                localSource.setBuffer(voice.buffer);
                localSource.play();
            }

            @Override
            public void release() {
                localSource.delete();
            }

            @Override
            public Sound getLastPlayed() {
                return lastPlayed;
            }
        };
    }

    //private static SfxDataLineDaemon sfxDataLineDaemon;

    //invoker 线程调用，保留锁
    public synchronized static SfxController getSfxController(Mascot mascot) {
//        if (null == sfxDataLineDaemon) {
//            final SfxDataLineDaemon sfxDaemon = new SfxDataLineDaemon();
//            new Thread(sfxDaemon, "SfxLineDaemonForAll").start();
//            sfxDataLineDaemon = sfxDaemon;
//        }
        return new SfxController() {
            @Override
            public void sound(Sound sound) {
                //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            public void release() {
                //To change body of implemented methods use File | Settings | File Templates.
            }
        };
    }

}

