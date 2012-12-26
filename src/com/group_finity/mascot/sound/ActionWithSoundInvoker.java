package com.group_finity.mascot.sound;

import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;
import java.util.logging.Level;

/**
 * Created with IntelliJ IDEA.
 * User: magi
 * Date: 12-12-26
 * Time: 下午2:27
 */
final class ActionWithSoundInvoker extends Thread {
    final String resPath;
    final Runnable cmd;

    ActionWithSoundInvoker(String resPath, Runnable cmd) {
        super();
        this.cmd = cmd;
        this.resPath = resPath;//在自己的线程中再加载音频
    }

    @Override
    public void run() {
        try {
            voice:
            if (SoundFactory.voiceOn) {
                final SourceDataLine line;
                try {
                    line = AudioSystem.getSourceDataLine(SoundFactory.appAudioFormat);
                    line.open(SoundFactory.appAudioFormat, SoundFactory.bufferSize);
                } catch (LineUnavailableException e) {
                    SoundFactory.log.log(Level.WARNING, "系统混音资源不足。", e);
                    break voice;
                }
                final Sound curSound = SoundFactory.getSound(resPath);

                int curPos = 0;
                play:
                while (SoundFactory.voiceOn) {
                    final int len = line.available();
                    if (null != curSound && len > SoundFactory.bufferWriteThreshold) {
                        final int left = curSound.bytes.length - curPos;
                        final int size = len > left ? left : len;
                        line.write(curSound.bytes, curPos, size);
                        curPos += size;
                        line.start();
                        if (curPos >= curSound.bytes.length) {
                            break play;
                        }
                    }
                    try {
                        Thread.sleep(SoundFactory.sleepMSec);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                line.close();
            }
        } finally {
            if (null != cmd)
                cmd.run();//即使音频异常，也要执行
        }
    }
}
