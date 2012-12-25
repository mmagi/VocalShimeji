package com.group_finity.mascot;

import com.sun.istack.internal.NotNull;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.JarURLConnection;
import java.net.URL;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class SoundManager {
    private static final Logger log = Logger.getLogger(Manager.class.getName());
    static final HashMap<String, CachedSound> soundCache = new HashMap<String, CachedSound>();
    static final AudioFormat appAudioFormat = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, 44100, 16, 2, 4, 44100, false); //标准CD音质
    public static boolean voiceOn = true;
    public static boolean sfxOn = true;
    public static final int defaultVoicePriority = -10;

    //SourceLineDaemonCommonParams
    private static final int bufferSizeInMSec = 100;
    private static final int bufferSize = SoundManager.appAudioFormat.getFrameSize() * (int) (SoundManager.appAudioFormat.getFrameRate() * bufferSizeInMSec / 1000);
    private static final int sleepMSec = 40;
    private static final int bufferWriteThreshold = SoundManager.appAudioFormat.getFrameSize() * (int) (SoundManager.appAudioFormat.getFrameRate() * sleepMSec / 1000);
    /**
     * 某些特殊情况下，缓冲播放结束后还会卡循环，如果连续sleep这个次数以后，强制停止line;
     */
    private static final int forceStopSleepCount = bufferSizeInMSec / sleepMSec;
    //从文件目录遍历
    //    public static void preLoadSounds() {
    //        File soundDir = new File("media");
    //        File[] soundList = soundDir.listFiles(new FilenameFilter() {
    //            @Override
    //            public boolean accept(File dir, String name) {
    //                return (name.endsWith(".wav") || name.endsWith(".mp3")) && !name.startsWith("#");
    //                //sound use only once like #init.wav not need to be cached,
    //                //place a '#' in front of it name to prevent pre-load
    //            }
    //        });
    //        for (File sound : soundList) {
    //            CachedSound cachedSound = new CachedSound();
    //            try {
    //                cachedSound.load(AudioSystem.getAudioInputStream(sound));
    //                soundCache.put("/" + sound.getName(), cachedSound);
    //            } catch (FileNotFoundException e) {
    //                log.log(Level.WARNING, "文件" + sound + "的不存在");
    //            } catch (UnsupportedAudioFileException e) {
    //                log.log(Level.WARNING, "文件" + sound + "的格式不支持");
    //            } catch (IOException e) {
    //                log.log(Level.WARNING, "文件" + sound + "读取时出错");
    //            }
    //        }
    //    }

    //从包含media/#wav目录的jar包遍历资源
    @SuppressWarnings("unchecked")
    public static void preLoadSounds() {
        try {
            final URL base = SoundManager.class.getResource("/media/#wav");
            final JarURLConnection jarURLConnection = (JarURLConnection) base.openConnection();
            final JarFile media = jarURLConnection.getJarFile();
            final Enumeration<JarEntry> enumeration = media.entries();
            while (enumeration.hasMoreElements()) {
                final JarEntry file = enumeration.nextElement();
                if (file.getName().startsWith("media/#wav/") && file.getName().endsWith(".wav")) {
                    final CachedSound cachedSound = new CachedSound();
                    cachedSound.load(AudioSystem.getAudioInputStream(SoundManager.class.getResource("/" + file.getName())));
                    soundCache.put(file.getName().substring(10), cachedSound);
                }
            }
            media.close();
        } catch (IOException e) {
            log.log(Level.WARNING, "音频文件读取时出错", e);
        } catch (UnsupportedAudioFileException e) {
            log.log(Level.WARNING, "音频文件格式错误", e);
        }
    }

    public static CachedSound getSound(String resPath) {
        return soundCache.get(resPath);
        //lazy load produce a delay at the loading time, not acceptable ----aki
        //changed from lazy loading to pre-load all sound, just get one
        /*
        synchronized (soundCache) {
            sound = soundCache.get(resPath);
            if (null == sound) {
                sound = new CachedSound();
                soundCache.put(resPath, sound);
            }
        }
        if ( !sound.ready && resPath != null ){
            URL res = Manager.class.getResource(resPath);
            if (null != res) try {
                sound.load(AudioSystem.getAudioInputStream(res));
            } catch (UnsupportedAudioFileException e) {
                log.log(Level.WARNING,"文件"+resPath+"的格式不支持");
            } catch (IOException e) {
                log.log(Level.WARNING,"文件"+resPath+"读取时出错");
            }
        }
        return sound;
        */
    }

    //    @Deprecated //一个Clip对应一个不自动关闭的线程，不建议使用
    //    public static Clip getClip(String resPath) {
    //        CachedSound sound = getSound(resPath);
    //        Clip clip = null;
    //        if (null != sound) {
    //            try {
    //                clip = AudioSystem.getClip();
    //                clip.open(appAudioFormat, sound.bytes, 0, sound.length);
    //            } catch (LineUnavailableException e) {
    //                log.warning("音频设备无法使用");
    //            }
    //        }
    //        return clip;
    //    }

    public static void invokeAfterSound(String resPath, Runnable cmd) {
        new Invoker(resPath, cmd).start();
    }

    public static VoiceDataLineDaemon getVoiceDataLineDaemon(Mascot mascot) {
        final VoiceDataLineDaemon voiceDaemon = new VoiceDataLineDaemon(mascot);
        new Thread(voiceDaemon, "DataLineDaemon" + mascot.toString()).start();
        return voiceDaemon;
    }

    private static SfxDataLineDaemon sfxDataLineDaemon;

    public synchronized static SfxDataLineDaemon getSfxDataLineDaemon(Mascot mascot) {
        if (null == sfxDataLineDaemon) {
            final SfxDataLineDaemon sfxDaemon = new SfxDataLineDaemon();
            ;
            new Thread(sfxDaemon, "SfxLineDaemonForAll").start();
            sfxDataLineDaemon = sfxDaemon;
        }
        return sfxDataLineDaemon;
    }

    public static final class CachedSound {
        boolean ready = false;
        byte[] bytes;
        int length;

        private synchronized boolean load(@NotNull AudioInputStream in) {
            if (ready)
                return true;
            if (null != in) {
                AudioFormat baseFormat = in.getFormat();
                if (!baseFormat.matches(appAudioFormat)) {
                    AudioFormat decodedFormat = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, baseFormat.getSampleRate(), 16, baseFormat.getChannels(), baseFormat.getChannels() * 2, baseFormat.getSampleRate(), false);
                    in = AudioSystem.getAudioInputStream(decodedFormat, in);
                    if (!decodedFormat.matches(appAudioFormat)) {
                        in = AudioSystem.getAudioInputStream(appAudioFormat, in);
                    }
                }
            }
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            byte[] temp = new byte[1024];
            int size = 0;
            try {
                while ((size = in.read(temp)) != -1) {
                    out.write(temp, 0, size);
                }
            } catch (IOException e) {
                log.log(Level.WARNING, "载入音频文件异常", e);
                return false;
            }
            length = out.size();
            bytes = out.toByteArray();
            ready = true;
            try {
                in.close();
            } catch (IOException e) {
                e.printStackTrace();//not care
            }
            return true;
        }
    }

    private static final class Invoker extends Thread {
        String resPath;
        Runnable cmd;

        Invoker(String resPath, Runnable cmd) {
            super();
            this.cmd = cmd;
            this.resPath = resPath;
        }

        @Override
        public void run() {
            voice:
            if (voiceOn) {
                final SourceDataLine line;

                try {
                    line = AudioSystem.getSourceDataLine(appAudioFormat);
                    line.open(appAudioFormat, bufferSize);
                } catch (LineUnavailableException e) {
                    log.log(Level.WARNING, "系统混音资源不足。");
                    log.log(Level.WARNING, e.toString());
                    break voice;
                }
                final CachedSound curSound = getSound(resPath);

                int curPos = 0;
                play:
                while (voiceOn) {
                    final int len = line.available();
                    if (null != curSound && len > bufferWriteThreshold) {
                        final int left = curSound.length - curPos;
                        final int size = len > left ? left : len;
                        line.write(curSound.bytes, curPos, size);
                        curPos += size;
                        line.start();
                        if (curPos >= curSound.length) {
                            break play;
                        }
                    }
                    try {
                        Thread.sleep(sleepMSec);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                line.close();
            }
            if (null != cmd)
                cmd.run();
        }
    }

    public static final class VoiceDataLineDaemon implements Runnable {

        private final Mascot mascot;
        private String voiceToPlay;
        private String lastPlayed;
        private int curLevel; // 按绝对值比较大小：负数表示同级可互相打断 正数表示同级声音不互相打断 0=没有声音在播放

        public String getLastPlayed() {
            return lastPlayed;
        }

        public VoiceDataLineDaemon(Mascot mascot) {
            this.mascot = mascot;
        }

        //主线程中调用
        public synchronized void speak(final String resPath, final int pri) {
            int priority = Math.abs(pri);
            if (priority > curLevel || (priority == curLevel && pri < 0)) {
                curLevel = priority;
                voiceToPlay = resPath;
            }
        }

        @Override
        public final void run() {
            final SourceDataLine line;
            try {
                line = AudioSystem.getSourceDataLine(appAudioFormat);
                line.open(appAudioFormat, bufferSize);
            } catch (LineUnavailableException e) {
                log.log(Level.WARNING, "系统混音资源不足，哑巴十四一匹。" + mascot.toString());
                log.log(Level.WARNING, e.toString());
                return;
            }

            final byte[] fadeBuffer = new byte[bufferWriteThreshold];

            CachedSound curSound = null;
            int curPos = 0;

            int sleepCount = 0;

            while (alive) {
                if (!voiceOn) {
                    line.stop();
                    line.flush();
                    curSound = null;
                    try {
                        Thread.sleep(sleepMSec);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                } else {
                    final String nextPlay = voiceToPlay;
                    voiceToPlay = null;
                    changeSound:
                    if (null != nextPlay) {
                        final CachedSound nextSound = getSound(nextPlay);
                        lastPlayed = nextPlay;
                        //                        if (curSound != null && curSound == nextSound) { //同一个音频自己不打断自己，直接忽略掉？ //不忽略照样打断，通过优化配置文件避免。
                        //                            break changeSound;
                        //                        } else
                        if (curSound != null && nextSound != null) { //换轨时不flush，做fadeInAndOut
                            final int left = curSound.length - curPos;
                            int fadeLength;//=min(bufferWriteThreshold,剩余长度,下一个音频长度)
                            fadeLength = nextSound.length > bufferWriteThreshold ? bufferWriteThreshold : nextSound.length;
                            fadeLength = fadeLength > left ? left : fadeLength;
                            for (int i = 0; i < fadeLength; i += 2) { // 2 byte for 16bit sound format
                                final int input1 = (curSound.bytes[curPos + i] & 0xff) + ((curSound.bytes[curPos + i + 1]) << 8);//for little-endian sound format
                                final int input2 = (nextSound.bytes[i] & 0xff) + ((nextSound.bytes[i + 1]) << 8);// little-endian，highest byte not mask for signed value
                                final double r = i / (double) fadeLength;
                                final int out = (int) (((1.0 - r) * input1) + r * input2);//liner fade
                                fadeBuffer[i] = (byte) (out & 0xff);
                                fadeBuffer[i + 1] = (byte) ((out >> 8) & 0xff);
                            }
                            while (line.available() < fadeLength) {
                                Thread.yield();
                            }
                            line.write(fadeBuffer, 0, fadeLength);
                            line.start();
                            curSound = nextSound;
                            curPos = fadeLength;
                            // fade完成
                        } else {
                            curSound = nextSound;
                            curPos = 0;
                        }
                    }
                    //end of change sound
                    int len;
                    if (null != curSound) {
                        while ((len = line.available()) < bufferWriteThreshold)
                            Thread.yield();
                        final int left = curSound.length - curPos;
                        final int size = len > left ? left : len;
                        line.write(curSound.bytes, curPos, size);
                        line.start();
                        curPos += size;
                        if (curPos >= curSound.length) { //换轨时不做flush，因此缓冲写完就判定为播放完成，不在意实际是否播放完
                            curSound = null;
                            sleepCount = forceStopSleepCount;
                            curLevel = 0;
                        }
                    } else {
                        try {
                            Thread.sleep(sleepMSec);
                            if (null == curSound) {
                                if (--sleepCount == 0)
                                    line.flush();
                            }
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
            line.close();
        }

        private boolean alive = true;

        public final void stop() {
            alive = false;
        }
    }

    public static final class SfxDataLineDaemon implements Runnable {
        private static final class sfxLine {
            String nextRes;
            CachedSound curSound;
            int curPos;
            SourceDataLine line;

            sfxLine() {
                try {
                    line = AudioSystem.getSourceDataLine(appAudioFormat);
                    line.open(appAudioFormat, bufferSize);
                } catch (LineUnavailableException e) {
                    log.log(Level.WARNING, "音频资源不足无音效", e);
                }
            }
        }

        final ConcurrentLinkedQueue<sfxLine> availableLines = new ConcurrentLinkedQueue<sfxLine>();
        final ConcurrentLinkedQueue<sfxLine> busyLines = new ConcurrentLinkedQueue<sfxLine>();
        private static final int totalLines = 10;

        SfxDataLineDaemon() {
            for (int i = 0; i < totalLines; i++) {
                availableLines.offer(new sfxLine());
            }
        }

        @Override
        public void run() {
            while (true) {
                if (sfxOn) {
                    Iterator<sfxLine> it = busyLines.iterator();
                    while (it.hasNext() && sfxOn) {
                        final sfxLine line = it.next();
                        synchronized (line) {
                            if (null != line.nextRes) {
                                line.curSound = getSound(line.nextRes);
                                line.nextRes = null;
                                line.curPos = 0;
                            }
                            int len;
                            if (null != line.curSound && (len = line.line.available()) > bufferWriteThreshold) {
                                final int left = line.curSound.length - line.curPos;
                                len = len > left ? left : len;
                                line.line.write(line.curSound.bytes, line.curPos, len);
                                line.line.start();
                                line.curPos += len;
                                if (line.curPos >= line.curSound.length) {
                                    busyLines.remove(line);
                                    line.curPos = 0;
                                    line.nextRes = null;
                                    line.curSound = null;
                                    availableLines.offer(line);
                                }
                            }
                        }
                    }
                }
                try {
                    Thread.sleep(sleepMSec);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

        public synchronized void sound(final String resPath) {
            sfxLine line;
            if ((line = availableLines.poll()) != null) {
                synchronized (line) {
                    line.nextRes = resPath;
                    busyLines.offer(line);
                }
            }//else too busy ignore this request
        }

        public void stop() {
            //改用共享音效线程，不用停止，忽略
        }
    }
    //    //每只十四独享音频线程，资源占用略大，改为共享
    //    public static final class SfxDataLineDaemon implements Runnable {
    //
    //        private final Mascot mascot;
    //        private final String[] voiceToPlay = new String[2];
    //        private boolean nextLine = true;
    //
    //        public SfxDataLineDaemon(Mascot mascot) {
    //            this.mascot = mascot;
    //        }
    //
    //        //主线程中调用
    //        public synchronized void sound(final String resPath) {
    //            if (nextLine) {
    //                voiceToPlay[0] = resPath;
    //                nextLine = false;
    //            } else {
    //                voiceToPlay[1] = resPath;
    //                nextLine = true;
    //            }
    //        }
    //
    //        @Override
    //        public final void run() {
    //            final SourceDataLine line0, line1;
    //            try {
    //                line0 = AudioSystem.getSourceDataLine(appAudioFormat);
    //                line0.open(appAudioFormat, bufferSize);
    //                line1 = AudioSystem.getSourceDataLine(appAudioFormat);
    //                line1.open(appAudioFormat, bufferSize);
    //            } catch (LineUnavailableException e) {
    //                log.log(Level.WARNING, "系统混音资源不足，无音效十四一匹。" + mascot.toString());
    //                log.log(Level.WARNING, e.toString());
    //                return;
    //            }
    //
    //            CachedSound curSound0 = null, curSound1 = null;
    //            int curPos0 = 0, curPos1 = 0;
    //
    //            int sleepCount0 = 0, sleepCount1 = 0;
    //
    //            while (alive) {
    //                if (!sfxOn) {
    //                    line0.stop();
    //                    line1.stop();
    //                    line0.flush();
    //                    line1.flush();
    //                    curSound0 = null;
    //                    curSound1 = null;
    //                    try {
    //                        Thread.sleep(sleepMSec);
    //                    } catch (InterruptedException e) {
    //                        e.printStackTrace();
    //                    }
    //                } else {
    //                    final String nextPlay0 = voiceToPlay[0];
    //                    voiceToPlay[0] = null;
    //                    changeSound:
    //                    if (null != nextPlay0) {
    //                        final CachedSound nextSound = getSound(nextPlay0);
    //                        curSound0 = nextSound;
    //                        curPos0 = 0;
    //                    }
    //                    final String nextPlay1 = voiceToPlay[1];
    //                    voiceToPlay[1] = null;
    //                    changeSound:
    //                    if (null != nextPlay1) {
    //                        final CachedSound nextSound = getSound(nextPlay1);
    //                        curSound1 = nextSound;
    //                        curPos1 = 0;
    //                    }
    //                    final int len0;
    //                    if (null != curSound0 && (len0 = line0.available()) >= bufferWriteThreshold) {
    //                        final int left = curSound0.length - curPos0;
    //                        final int size = len0 > left ? left : len0;
    //                        line0.write(curSound0.bytes, curPos0, size);
    //                        line0.start();
    //                        curPos0 += size;
    //                        if (curPos0 >= curSound0.length) {
    //                            sleepCount0 = forceStopSleepCount;
    //                        }
    //                    }
    //                    final int len1;
    //                    if (null != curSound1 && (len1 = line1.available()) >= bufferWriteThreshold) {
    //                        final int left = curSound1.length - curPos1;
    //                        final int size = len1 > left ? left : len1;
    //                        line1.write(curSound1.bytes, curPos1, size);
    //                        line1.start();
    //                        curPos1 += size;
    //                        if (curPos1 >= curSound1.length) {
    //                            sleepCount1 = forceStopSleepCount;
    //                        }
    //                    }
    //                    try {
    //                        Thread.sleep(sleepMSec);
    //                        if (null == curSound0) {
    //                            if (--sleepCount0 == 0)
    //                                line0.flush();
    //                        }
    //                        if (null == curSound1) {
    //                            if (--sleepCount1 == 0)
    //                                line1.flush();
    //                        }
    //                    } catch (InterruptedException e) {
    //                        e.printStackTrace();
    //                    }
    //                }
    //            }
    //            line0.close();
    //            line1.close();
    //        }
    //
    //        private boolean alive = true;
    //
    //        public final void stop() {
    //            alive = false;
    //        }
    //
    //
    //    }

}

