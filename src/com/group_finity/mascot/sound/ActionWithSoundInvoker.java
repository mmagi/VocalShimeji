package com.group_finity.mascot.sound;

import com.group_finity.mascot.util.QueList;

import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;
import java.util.Iterator;
import java.util.logging.Level;

/**
 * Created with IntelliJ IDEA.
 * User: magi
 * Date: 12-12-26
 * Time: 下午2:27
 */
final class ActionWithSoundInvoker extends Thread {
    class Task {
        Sound curSound;
        Runnable cmd;
        int curPos;
        boolean done;
        SourceDataLine line;

        Task(final Sound sound, final Runnable cmd) {
            this.curSound = sound;
            this.cmd = cmd;
            this.curPos = 0;
            this.done = false;
            try {
                this.line = AudioSystem.getSourceDataLine(SoundFactory.appAudioFormat);
                this.line.open(SoundFactory.appAudioFormat, SoundFactory.defaultBufferSize);
            } catch (LineUnavailableException e) {
                SoundFactory.log.log(Level.WARNING, "音频资源不足无音效", e);
                this.curSound = null;
            }
        }

        final void updateLine() {
//            final int len;
//            if (null != curSound && (len = line.available()) >= SoundFactory.defaultWriteThreshold) {
//                final Sound curSound = this.curSound;
//                final int curPos = this.curPos;
//                final int left = curSound.bytes.length - curPos;
//                final int size = len > left ? left : len;
//                if (!line.isActive())
//                    line.start();
//                line.write(curSound.bytes, curPos, size);
//                this.curPos += size;
//                if (this.curPos >= curSound.bytes.length) {
//                    this.curSound = null;
//                }
//            }
        }
    }

    QueList<Task> tasks = new QueList<Task>();

    void Invoke(Sound sound, Runnable cmd) {
        tasks.offer(new Task(sound, cmd));
    }

    @Override
    public void run() {
        for (final Iterator<Task> iterator = tasks.asCircle().iterator(); iterator.hasNext(); ) {
            final Task task = iterator.next();
            if (null == task) {
                try {
                    Thread.sleep(SoundFactory.defaultSleepMSec);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            } else {
                if (!SoundFactory.voiceOn || null == task.curSound) {
                    try {
                        task.cmd.run();
                        task.line.close();
                    } catch (Throwable e) {
                        SoundFactory.log.log(Level.WARNING, "执行命令时异常", e);
                    } finally {
                        iterator.remove();
                    }
                } else {
                    try {
                        task.updateLine();
                    } catch (Throwable e) {
                        SoundFactory.log.log(Level.WARNING, "执行声音时异常", e);
                        try {
                            task.curSound = null;
                        } catch (Throwable ignored) {
                        }
                    }
                }
            }
        }
    }
}
