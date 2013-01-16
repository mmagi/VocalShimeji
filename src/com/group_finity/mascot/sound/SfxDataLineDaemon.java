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
public final class SfxDataLineDaemon implements Runnable, SfxController {
    private static final class sfxLine {
        volatile boolean busy = false;
        volatile Sound curSound;
        volatile int curPos;
        SourceDataLine line;

        sfxLine() {
            try {
                line = AudioSystem.getSourceDataLine(SoundFactory.appAudioFormat);
                line.open(SoundFactory.appAudioFormat, SoundFactory.defaultBufferSize);
            } catch (LineUnavailableException e) {
                SoundFactory.log.log(Level.WARNING, "音频资源不足无音效", e);
            }
        }
    }

    final ConcurrentLinkedQueue<sfxLine> availableLines = new ConcurrentLinkedQueue<sfxLine>();
    private static final int totalLines = 50;
    final sfxLine[] lines = new sfxLine[totalLines];

    SfxDataLineDaemon() {
        for (int i = 0; i < totalLines; i++) {
            availableLines.offer(lines[i] = new sfxLine());
        }
    }

    @Override
    public void run() {
        //noinspection InfiniteLoopStatement
        while (true) {
            if (SoundFactory.sfxOn) {
                for (final sfxLine line : lines) {
                    int len;
                    if (SoundFactory.sfxOn && line.busy && (len = line.line.available()) >= SoundFactory.defaultWriteThreshold) {
                        if (null != line.curSound ) {
                            final int left = line.curSound.bytes.length - line.curPos;
                            len = len > left ? left : len;
                            line.line.write(line.curSound.bytes, line.curPos, len);
                            line.curPos += len;
                            if (line.curPos >= line.curSound.bytes.length) {
                                line.curSound = null;
                                line.curPos = 0;
                            }
                        }else {
                            line.line.write(SoundFactory.silence,0,SoundFactory.defaultWriteThreshold);
                            line.busy = false;
                            availableLines.offer(line);
                        }
                    }
                }
                try {
                    Thread.sleep(SoundFactory.defaultSleepMSec);
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
                        Thread.sleep(SoundFactory.defaultSleepMSec);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    public void sound(final Sound sfx) {
        sfxLine line;
        if ((line = availableLines.poll()) != null) {
            line.curSound = sfx;
            if (!line.line.isActive()) line.line.start();
            line.busy = true;
        }//else too busy ignore this request
    }

    @SuppressWarnings("EmptyMethod")
    //共享音效line，不用释放，忽略
    @Override
    public void release() {
    }

}
