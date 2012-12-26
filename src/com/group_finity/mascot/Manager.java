package com.group_finity.mascot;

import com.group_finity.mascot.config.Configuration;
import com.group_finity.mascot.exception.BehaviorInstantiationException;
import com.group_finity.mascot.exception.CantBeAliveException;

import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Manager {
    private static final Logger log = Logger.getLogger(Manager.class.getName());
    public static final int TICK_INTERVAL = 40;
    private final ArrayList<Mascot> mascots = new ArrayList<Mascot>();

    private final Set<Mascot> added = new LinkedHashSet<Mascot>();

    private final Set<Mascot> removed = new LinkedHashSet<Mascot>();
    private boolean exitOnLastRemoved;
    private transient Thread thread;

    public Manager() {
    }

    public void start() {
        if ((this.thread != null) && (this.thread.isAlive())) {
            return;
        }

        this.thread = new Thread("tick-manager") {
            public void run() {
                long prev = System.nanoTime() / 1000000L;
                try {
                    while (true) {
                        long cur = System.nanoTime() / 1000000L;
                        if (cur - prev >= 40L) {
                            if (cur > prev + 80L)
                                prev = cur;
                            else
                                prev += 40L;
                        } else {
                            Thread.sleep(1L, 0);
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

        synchronized (getMascots()) {
            for (Mascot mascot : getAdded()) {
                getMascots().add(mascot);
            }
            getAdded().clear();

            for (Mascot mascot : getRemoved()) {
                getMascots().remove(mascot);
            }
            getRemoved().clear();

            if (!Gintama.disable) {
                if (!Gintama.active && getMascots().size() == 52)
                    Gintama.active(mascots);
            }

            for (Mascot mascot : getMascots()) {
                mascot.tick();
            }

            for (Mascot mascot : getMascots()) {
                mascot.apply();
            }

        }

        if ((isExitOnLastRemoved()) && (getMascots().size() == 0)) {
            Main.getInstance().exit();
        }

    }

    public void add(Mascot mascot) {
        synchronized (getAdded()) {
            getAdded().add(mascot);
            getRemoved().remove(mascot);
        }
        mascot.setManager(this);
    }

    public void remove(Mascot mascot) {
        synchronized (getAdded()) {
            getAdded().remove(mascot);
            getRemoved().add(mascot);
        }
        mascot.setManager(null);
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
                ((Mascot) getMascots().get(i)).dispose();
        }
    }

    public void disposeAll() {
        synchronized (getMascots()) {
            for (int i = getMascots().size() - 1; i >= 0; i--)
                ((Mascot) getMascots().get(i)).dispose();
        }
    }

    public int getCount() {
        synchronized (getMascots()) {
            return getMascots().size();
        }
    }

    public void setExitOnLastRemoved(boolean exitOnLastRemoved) {
        this.exitOnLastRemoved = exitOnLastRemoved;
    }

    public boolean isExitOnLastRemoved() {
        return this.exitOnLastRemoved;
    }

    private final List<Mascot> getMascots() {
        return this.mascots;
    }

    private Set<Mascot> getAdded() {
        return this.added;
    }

    private Set<Mascot> getRemoved() {
        return this.removed;
    }
}