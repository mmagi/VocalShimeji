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
    final Sound sound;
    final Runnable cmd;

    ActionWithSoundInvoker(Sound sound, Runnable cmd) {
        super();
        this.cmd = cmd;
        this.sound = sound;//在自己的线程中再加载音频
    }

    @Override
    public void run() {
        try {
            voice:
            if (SoundFactory.voiceOn && null != sound) {
                final SourceDataLine line;
                try {
                    line = AudioSystem.getSourceDataLine(SoundFactory.appAudioFormat);
                    line.open(SoundFactory.appAudioFormat, SoundFactory.bufferSize);
                } catch (LineUnavailableException e) {
                    SoundFactory.log.log(Level.WARNING, "系统混音资源不足。", e);
                    break voice;
                }
                int curPos = 0;
                play:
                while (SoundFactory.voiceOn) {
                    final int len = line.available();
                    if (len >= SoundFactory.bufferWriteThreshold) {
                        final int left = sound.bytes.length - curPos;
                        final int size = len > left ? left : len;
                        line.write(sound.bytes, curPos, size);
                        curPos += size;
                        line.start();
                        if (curPos >= sound.bytes.length) {
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
