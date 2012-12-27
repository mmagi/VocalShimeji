package com.group_finity.mascot.sound;

import com.group_finity.mascot.Manager;
import com.group_finity.mascot.Mascot;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class SoundFactory {
    static final Logger log = Logger.getLogger(Manager.class.getName());
    static final ConcurrentHashMap<String, Sound> soundCache = new ConcurrentHashMap<String, Sound>();
    static final AudioFormat appAudioFormat = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, 44100, 16, 2, 4, 44100, false); //标准CD音质
    public static boolean voiceOn = true;
    public static boolean sfxOn = true;
    public static final int defaultVoicePriority = -10;

    //CommonParams For Deamon
    static final int bufferSizeInMSec = 500;
    static final int bufferSize = SoundFactory.appAudioFormat.getFrameSize() * (int) (SoundFactory.appAudioFormat.getFrameRate() * bufferSizeInMSec / 1000);
    static final int sleepMSec = 100;
    static final int bufferWriteThreshold = SoundFactory.appAudioFormat.getFrameSize() * (int) (SoundFactory.appAudioFormat.getFrameRate() * sleepMSec / 1200);
    static final int forceStopSleepCount = bufferSizeInMSec / sleepMSec;//某些特殊情况下，缓冲播放结束后还会卡循环，如果连续sleep这个次数以后，强制停止line;


    public static final Sound getSound(String resPath) {
        if (null == resPath) return null;
        Sound sound = soundCache.get(resPath);
        if (null == sound) {
            try {
                sound = new Sound(AudioSystem.getAudioInputStream(SoundFactory.class.getResource("/media" + resPath)));
                soundCache.put(resPath, sound);
            } catch (UnsupportedAudioFileException e) {
                log.log(Level.WARNING, "音频文件{0}的格式不支持", resPath);
            } catch (Exception e) {
                log.log(Level.WARNING, "载入音频文件{0}时出错", resPath);
            }
        }
        return sound;
    }

    public static void invokeAfterSound(Sound sound, Runnable cmd) {
        new ActionWithSoundInvoker(sound, cmd).start();
    }

    private static VoiceDataLineDaemon voiceDataLineDaemon;

    public synchronized static VoiceController getVoiceController(Mascot mascot) {
        if (null == voiceDataLineDaemon) {
            final VoiceDataLineDaemon voiceDaemon = new VoiceDataLineDaemon();
            new Thread(voiceDaemon, "VoiceLineDaemonForAll").start();
            voiceDataLineDaemon = voiceDaemon;
        }
        return voiceDataLineDaemon.createVoiceLine();
    }

    private static SfxDataLineDaemon sfxDataLineDaemon;

    //invoker 线程调用，保留锁
    public synchronized static SfxController getSfxController(Mascot mascot) {
        if (null == sfxDataLineDaemon) {
            final SfxDataLineDaemon sfxDaemon = new SfxDataLineDaemon();
            new Thread(sfxDaemon, "SfxLineDaemonForAll").start();
            sfxDataLineDaemon = sfxDaemon;
        }
        return sfxDataLineDaemon;
    }

}

