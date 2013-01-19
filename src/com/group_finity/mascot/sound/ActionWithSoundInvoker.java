package com.group_finity.mascot.sound;

import com.group_finity.mascot.util.QueList;

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
        Runnable cmd;
        boolean done;
        SoundSource localSource;

        Task(final SoundBuffer sound, final Runnable cmd) {
            try {
                localSource = new SoundSource();
                localSource.setBuffer(sound);
                localSource.setPosition(0.0F, 0.0F, 0.0F);
                localSource.setLooping(false);
                this.done = false;
            } catch (Throwable e) {
                this.done = true;
            }
            this.cmd = cmd;
        }

        final void start() {
            localSource.play();
        }

        final void updateLine() {
            this.done = localSource == null || !localSource.isPlaying();
        }

        final void release() {
            localSource.delete();
        }
    }

    QueList<Task> tasks = new QueList<Task>();

    void Invoke(SoundBuffer sound, Runnable cmd) {
        final Task task = new Task(sound, cmd);
        tasks.offer(task);
        task.start();
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
                if (!SoundFactory.voiceOn || task.done) {
                    try {
                        task.cmd.run();
                    } catch (Throwable e) {
                        SoundFactory.log.log(Level.WARNING, "执行命令时异常", e);
                    } finally {
                        iterator.remove();
                        try {
                            task.release();
                        } catch (Throwable ignored) {
                        }
                    }
                } else {
                    try {
                        task.updateLine();
                    } catch (Throwable e) {
                        SoundFactory.log.log(Level.WARNING, "执行声音时异常", e);
                        try {
                            task.done = true;
                        } catch (Throwable ignored) {
                        }
                    }
                }
            }
        }
    }
}
