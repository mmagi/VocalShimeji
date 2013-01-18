package com.group_finity.mascot;

import com.group_finity.mascot.config.Configuration;
import com.group_finity.mascot.exception.BehaviorInstantiationException;
import com.group_finity.mascot.exception.CantBeAliveException;
import com.group_finity.mascot.util.QueList;

import javax.swing.*;
import java.util.Iterator;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Manager {
    private static final Logger log = Logger.getLogger(Manager.class.getName());
    private final AtomicInteger mascotCount = new AtomicInteger(0);
    private final QueList<Mascot> mascotList = new QueList<Mascot>();
    private boolean alive = true;

    public Manager() {
    }

    public void start() {
        new Thread(new Runnable() {
            public void run() {
                long prev = System.nanoTime();
                try {
                    while (notExitOnLastRemoved()) {
                        long cur = System.nanoTime();
                        if (cur - prev >= 40 * 1000000L) {
                            if (cur > prev + 80 * 1000000L)
                                prev = cur;
                            else
                                prev += 40 * 1000000L;
                        } else {
                            Thread.sleep(1L);
                            continue;
                        }

                        NativeFactory.getInstance().getEnvironment().tick();

                        for (final Iterator<Mascot> iterator = mascotList.iterator(); iterator.hasNext(); ) {
                            final Mascot mascot = iterator.next();
                            if (mascot.isDisposed()) {
                                iterator.remove();
                            } else {
                                mascot.tick();
                            }
                        }
                    }
                    Main.getInstance().exit();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }, "TickManager").start();

        new Thread(new Runnable() {
            @Override
            public void run() {
                for (final Mascot mascot : mascotList.asCircle()) {
                    long prev = System.nanoTime();
                    if (null != mascot) {
                        mascot.apply();
                    } else {
                        final long cur = System.nanoTime();
                        final long sleeptime = 40 * 1000000L - cur + prev;
                        if (sleeptime > 0) try {
                            Thread.sleep(sleeptime / 1000000L);
                        } catch (InterruptedException ignored) {
                        }
                        else Thread.yield();
                    }
                }
            }
        }, "MascotPainter").start();
    }

    public void add(Mascot mascot) {
        mascot.setManager(this);
        mascotList.offer(mascot);
        mascotCount.getAndIncrement();
//        if (!Gintama.disable) {
//                if (!Gintama.active && mascotCount.get() == 52)
//                    Gintama.active(mascotList);
//        }
    }

    public void remove(Mascot mascot) {
        mascot.getWindow().dispose();
        mascotCount.getAndDecrement();
    }

    public void setBehaviorAll(Configuration configuration, String name) {
        for (final Mascot mascot : mascotList) {
            try {
                mascot.setBehavior(configuration.buildBehavior(name));
            } catch (BehaviorInstantiationException e) {
                log.log(Level.SEVERE, "次の行動の初期化に失敗しました", e);
                mascot.dispose();
            } catch (CantBeAliveException e) {
                log.log(Level.SEVERE, "生き続けることが出来ない状況", e);
                mascot.dispose();
            }
        }
    }

    public void remainOne() {
        final Iterator<Mascot> iterator = mascotList.iterator();
        if (iterator.hasNext()) iterator.next();
        while (iterator.hasNext()) {
            iterator.next().dispose();
        }
    }

    public void disposeAll() {
        for (final Mascot mascot : mascotList) {
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    mascot.dispose();
                }
            });
        }
    }

    public int getCount() {
        return mascotCount.get();
    }

    public void setExitOnLastRemoved(boolean exitOnLastRemoved) {
        this.alive = !exitOnLastRemoved;
    }

    public boolean notExitOnLastRemoved() {
        return this.alive;
    }

}