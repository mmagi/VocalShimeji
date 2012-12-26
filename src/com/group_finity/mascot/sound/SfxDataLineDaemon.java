package com.group_finity.mascot.sound;

import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.Level;

/**
 * Created with IntelliJ IDEA.
 * User: magi
 * Date: 12-12-26
 * Time: 下午2:25
 */
public final class SfxDataLineDaemon implements Runnable {
    private static final class sfxLine {
        boolean busy = false;
        Sound curSound;
        int curPos;
        SourceDataLine line;

        sfxLine() {
            try {
                line = AudioSystem.getSourceDataLine(SoundFactory.appAudioFormat);
                line.open(SoundFactory.appAudioFormat, SoundFactory.bufferSize);
            } catch (LineUnavailableException e) {
                SoundFactory.log.log(Level.WARNING, "音频资源不足无音效", e);
            }
        }
    }

    final ConcurrentLinkedQueue<sfxLine> availableLines = new ConcurrentLinkedQueue<sfxLine>();
    private static final int totalLines = 10;
    final sfxLine[] lines = new sfxLine[totalLines];

    SfxDataLineDaemon() {
        for (int i = 0; i < totalLines; i++) {
            availableLines.offer(lines[i] = new sfxLine());
        }
    }

    @Override
    public void run() {
        while (true) {
            if (SoundFactory.sfxOn) {
                for (final sfxLine line : lines) {
                    if (SoundFactory.sfxOn && line.busy) {
                        int len;
                        if (null != line.curSound && (len = line.line.available()) >= SoundFactory.bufferWriteThreshold) {
                            final int left = line.curSound.bytes.length - line.curPos;
                            len = len > left ? left : len;
                            line.line.write(line.curSound.bytes, line.curPos, len);
                            line.line.start();
                            line.curPos += len;
                            if (line.curPos >= line.curSound.bytes.length) {
                                line.busy = false;
                                line.curPos = 0;
                                line.curSound = null;
                                availableLines.offer(line);
                            }
                        }
                    }
                }
                try {
                    Thread.sleep(SoundFactory.sleepMSec);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            } else {
                for (final sfxLine line : lines) {
                    line.line.stop();
                    line.line.flush();
                }
                while (!SoundFactory.sfxOn) {
                    try {
                        Thread.sleep(SoundFactory.sleepMSec);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    //只有一个主线程调用，不需要锁
    public void sound(final Sound sfx) {
        sfxLine line;
        if ((line = availableLines.poll()) != null) {
            line.curSound = sfx;
            line.busy = true;
        }//else too busy ignore this request
    }

    public void stop() {
        //改用共享音效线程，不用停止，忽略
    }

}
