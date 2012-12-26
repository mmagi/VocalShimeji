package com.group_finity.mascot.sound;

import com.group_finity.mascot.Mascot;

import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;
import java.util.logging.Level;

/**
 * Created with IntelliJ IDEA.
 * User: magi
 * Date: 12-12-26
 * Time: 下午2:22
 */
public final class VoiceDataLineDaemon implements Runnable {

    private final Mascot mascot;
    private Sound voiceToPlay;
    private Sound lastPlayed;
    private int curLevel; // 按绝对值比较大小：负数表示同级可互相打断 正数表示同级声音不互相打断 0=没有声音在播放

    public Sound getLastPlayed() {
        return lastPlayed;
    }

    VoiceDataLineDaemon(Mascot mascot) {
        this.mascot = mascot;
    }

    //单一主线程中调用，去掉锁
    public void speak(final Sound voice, final int pri) {
        int priority = Math.abs(pri);
        if (priority > curLevel || (priority == curLevel && pri < 0)) {
            curLevel = priority;
            voiceToPlay = voice;
        }
    }

    @Override
    public final void run() {
        final SourceDataLine line;
        try {
            line = AudioSystem.getSourceDataLine(SoundFactory.appAudioFormat);
            line.open(SoundFactory.appAudioFormat, SoundFactory.bufferSize);
        } catch (LineUnavailableException e) {
            SoundFactory.log.log(Level.WARNING, "系统混音资源不足，哑巴十四一匹。" + mascot.toString());
            SoundFactory.log.log(Level.WARNING, e.toString());
            return;
        }

        final byte[] fadeBuffer = new byte[SoundFactory.bufferWriteThreshold];

        Sound curSound = null;
        int curPos = 0;

        int sleepCount = 0;

        while (alive) {
            if (!SoundFactory.voiceOn) {
                line.stop();
                line.flush();
                curSound = null;
                while (!SoundFactory.voiceOn) {
                    try {
                        Thread.sleep(SoundFactory.sleepMSec);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            } else {
                final Sound nextSound = voiceToPlay;
                voiceToPlay = null;
                changeSound:
                if (null != nextSound) {
                    lastPlayed = nextSound;
                    //                        if (curSound != null && curSound == nextSound) { //同一个音频自己不打断自己，直接忽略掉？ //不忽略照样打断，通过优化配置文件避免。
                    //                            break changeSound;
                    //                        } else
                    if (curSound != null && nextSound != null) { //换轨时不flush，做fadeInAndOut
                        final int left = curSound.bytes.length - curPos;
                        int fadeLength;//=min(bufferWriteThreshold,剩余长度,下一个音频长度)
                        fadeLength = nextSound.bytes.length > SoundFactory.bufferWriteThreshold ? SoundFactory.bufferWriteThreshold : nextSound.bytes.length;
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
                    while ((len = line.available()) < SoundFactory.bufferWriteThreshold)
                        Thread.yield();
                    final int left = curSound.bytes.length - curPos;
                    final int size = len > left ? left : len;
                    line.write(curSound.bytes, curPos, size);
                    line.start();
                    curPos += size;
                    if (curPos >= curSound.bytes.length) { //换轨时不做flush，因此缓冲写完就判定为播放完成，不在意实际是否播放完
                        curSound = null;
                        sleepCount = SoundFactory.forceStopSleepCount;
                        curLevel = 0;
                    }
                } else {
                    try {
                        Thread.sleep(SoundFactory.sleepMSec);
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
