package com.group_finity.mascot;

import com.group_finity.mascot.config.Configuration;
import com.group_finity.mascot.exception.BehaviorInstantiationException;
import com.group_finity.mascot.exception.CantBeAliveException;
import com.group_finity.mascot.util.QueList;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Manager {
    private static final Logger log = Logger.getLogger(Manager.class.getName());
    private final ArrayList<Mascot> mascots = new ArrayList<Mascot>();

    private final Set<Mascot> added = new LinkedHashSet<Mascot>();

    private final Set<Mascot> removed = new LinkedHashSet<Mascot>();

    private final AtomicInteger mascotCount = new AtomicInteger(0);
    private final QueList<Mascot> mascotList = new QueList<Mascot>();
    private boolean exitOnLastRemoved;
    private transient Thread thread;

    public Manager() {
    }

    public void start() {
        if ((this.thread != null) && (this.thread.isAlive())) {
            return;
        }

        this.thread = new Thread("TickManager") {
            public void run() {
                long prev = System.nanoTime();
                try {
                    //noinspection InfiniteLoopStatement
                    while (true) {
                        long cur = System.nanoTime();
                        if (cur - prev >= 40*1000000L) {
                            if (cur > prev + 80*1000000L)
                                prev = cur;
                            else
                                prev += 40*1000000L;
                        } else {
                            Thread.sleep(1L);
                            continue;
                        }

                        Manager.this.tick();
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        };
        this.thread.setDaemon(false);

        this.thread.start();
    }

    public void stop() {
        if ((this.thread == null) || (!this.thread.isAlive())) {
            return;
        }

        this.thread.interrupt();
        try {
            this.thread.join();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private void tick() {
        NativeFactory.getInstance().getEnvironment().tick();

        for (final Iterator<Mascot> iterator = mascotList.iterator(); iterator.hasNext(); ) {
            final Mascot mascot = iterator.next();
            if (null == mascot.getManager()){
                iterator.remove();
            }else {
                mascot.tick();
                mascot.apply();
            }
        }
            if (!Gintama.disable) {
                if (!Gintama.active && mascotCount.get() == 52)
                    Gintama.active(mascots);
            }

        if ((isExitOnLastRemoved())) {
            Main.getInstance().exit();
        }

    }

    public void add(Mascot mascot) {
        mascot.setManager(this);
        mascotList.offer(mascot);
        mascotCount.getAndIncrement();
    }

    public void remove(Mascot mascot) {
        synchronized (mascot){
            if (null != mascot.getManager()){
                mascotCount.getAndDecrement();
                mascot.setManager(null);
            }
        }
    }

    public void setBehaviorAll(Configuration configuration, String name) {
        Iterator i$;
        Mascot mascot;
        synchronized (getMascots()) {
            for (i$ = getMascots().iterator(); i$.hasNext(); ) {
                mascot = (Mascot) i$.next();
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
    }

    public void remainOne() {
        synchronized (getMascots()) {
            for (int i = getMascots().size() - 1; i > 0; i--)
                getMascots().get(i).dispose();
        }
    }

    public void disposeAll() {
        synchronized (getMascots()) {
            for (int i = getMascots().size() - 1; i >= 0; i--)
                getMascots().get(i).dispose();
        }
    }

    public int getCount() {
        return mascotCount.get();
//        synchronized (getMascots()) {
//            return getMascots().size();
//        }
    }

    public void setExitOnLastRemoved(boolean exitOnLastRemoved) {
        this.exitOnLastRemoved = exitOnLastRemoved;
    }

    public boolean isExitOnLastRemoved() {
        return this.exitOnLastRemoved;
    }

    private List<Mascot> getMascots() {
        return this.mascots;
    }

    private Set<Mascot> getAdded() {
        return this.added;
    }

    private Set<Mascot> getRemoved() {
        return this.removed;
    }
}