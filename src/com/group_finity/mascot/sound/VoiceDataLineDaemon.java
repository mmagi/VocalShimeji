package com.group_finity.mascot.sound;

import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.Level;

/**
 * Created with IntelliJ IDEA.
 * User: magi
 * Date: 12-12-26
 * Time: 下午2:22
 */
public final class VoiceDataLineDaemon implements Runnable {
    private static final class voiceLine implements VoiceController {
        final byte[] fadeBuffer = new byte[SoundFactory.defaultWriteThreshold];
        final SourceDataLine line;
        boolean alive;//初始化时用以是否成功获得资源，release时标记资源可回收到pool中;
        volatile Sound voiceToPlay;
        Sound lastPlayed;
        Sound curSound;
        int curPos;
        volatile int curLevel = 0; // 按绝对值比较大小：负数表示同级可互相打断 正数表示同级声音不互相打断 0=没有声音在播放

        public Sound getLastPlayed() {
            return lastPlayed;
        }

        voiceLine() {
            SourceDataLine line = null;
            try {
                line = AudioSystem.getSourceDataLine(SoundFactory.appAudioFormat);
                line.open(SoundFactory.appAudioFormat, SoundFactory.defaultBufferSize);
                alive = true;//标记初始化成功
            } catch (LineUnavailableException e) {
                SoundFactory.log.log(Level.WARNING, "音频资源不足无语音", e);
                alive = false;//标记初始化失败
            }
            this.line = line;
        }

        @Override
        public void speak(final Sound voice, final int pri) {
            int priority = Math.abs(pri);
            if (priority > curLevel || (priority == curLevel && pri < 0)) {
                curLevel = priority;
                voiceToPlay = voice;
            }
        }

        @Override
        public void release() {
            alive = false;
        }

        final void writeLine(){
            final int len;
            if (null != curSound && (len = line.available()) >= SoundFactory.defaultWriteThreshold) {
                final Sound curSound = this.curSound;
                final int curPos = this.curPos;
                final int left = curSound.bytes.length - curPos;
                final int size = len > left ? left : len;
                if (!line.isActive()) line.start();
                line.write(curSound.bytes, curPos, size);
                this.curPos += size;
                if (this.curPos >= curSound.bytes.length) { //换轨时不做flush，因此缓冲写完就判定为播放完成，不在意实际是否播放完
                    this.curSound = null;
                    this.curLevel = 0;
                }
            }
        }
        final void fadeSwitchSoundTo(final Sound nextSound) {
            lastPlayed = nextSound;
            if (this.curSound != null) { //换轨时不flush，做fadeInAndOut
                final Sound curSound = this.curSound;
                final int curPos = this.curPos;
                final byte[] fadeBuffer = this.fadeBuffer;
                final int left = curSound.bytes.length - curPos;
                int fadeLength;//=min(defaultWriteThreshold,剩余长度,下一个音频长度)
                fadeLength = nextSound.bytes.length > SoundFactory.defaultWriteThreshold ? SoundFactory.defaultWriteThreshold : nextSound.bytes.length;
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
                if (line.isActive()) line.start();
                line.write(fadeBuffer, 0, fadeLength);
                this.curSound = nextSound;
                this.curPos = fadeLength;
                // fade完成
            } else { //curSound == null
                this.curSound = nextSound;
                this.curPos = 0;
            }
        }
    }

    private static final ConcurrentLinkedQueue<voiceLine> linePool = new ConcurrentLinkedQueue<voiceLine>();
    private static final ArrayList<voiceLine> lines = new ArrayList<voiceLine>(100);

    protected voiceLine createVoiceLine() {
        voiceLine ret = linePool.peek();
        if (null == ret) {
            ret = new voiceLine();
            if (ret.alive) { //初始化成功，否则为哑巴十四，不加入资源list
                synchronized (lines) {
                    lines.add(ret);
                }
            }
        } else {
            ret.alive = true;
        }
        return ret;
    }

    @Override
    public final void run() {

        //noinspection InfiniteLoopStatement
        while (true) {
            if (SoundFactory.voiceOn) {
                for (int n = lines.size() - 1; n >= 0; n--) {
                    final voiceLine line = lines.get(n);
                    if (!SoundFactory.voiceOn) {
                        line.line.stop();
                        line.line.flush();
                    } else {
                        if (line.alive) {
                            final Sound nextSound = line.voiceToPlay;
                            line.voiceToPlay = null;
                            if (null != nextSound) {
                                line.fadeSwitchSoundTo(nextSound);
                            }
                            line.writeLine();
                        } else {
                            linePool.offer(line);
                        }
                    }
                }
                try {
                    Thread.sleep(SoundFactory.defaultSleepMSec);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            } else {
                for (final voiceLine line : lines) {
                    line.line.stop();
                    line.line.flush();
                }
                while (!SoundFactory.voiceOn) {
                    try {
                        Thread.sleep(SoundFactory.defaultSleepMSec);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

    }
}
