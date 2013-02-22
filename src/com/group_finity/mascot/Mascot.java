package com.group_finity.mascot;

import com.group_finity.mascot.behavior.Behavior;
import com.group_finity.mascot.environment.MascotEnvironment;
import com.group_finity.mascot.exception.CantBeAliveException;
import com.group_finity.mascot.image.MascotImage;
import com.group_finity.mascot.image.TranslucentWindow;
import com.group_finity.mascot.sound.SfxController;
import com.group_finity.mascot.sound.SoundFactory;
import com.group_finity.mascot.sound.VoiceController;

import java.awt.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Mascot {
    private static final Logger log = Logger.getLogger(Mascot.class.getName());
    private static final AtomicInteger lastId = new AtomicInteger();

    private final int id;
    private final TranslucentWindow window = NativeFactory.getInstance().newTransparentWindow();

    private Manager manager = null;

    private Point anchor = new Point(0, 0);

    private MascotImage image = null;

    private boolean lookRight = false;

    private Behavior behavior = null;

    private int time = 0;

    private boolean animating = true;

    private final MascotEnvironment environment = new MascotEnvironment(this);

    public final VoiceController voiceController;

    public final SfxController sfxController;

    public Mascot() {

        this.id = lastId.incrementAndGet();

        voiceController = SoundFactory.getVoiceController(this);

        sfxController = SoundFactory.getSfxController(this);

        log.log(Level.INFO, "マスコット生成({0})", this);

        getWindow().asJWindow().setAlwaysOnTop(true);

        getWindow().asJWindow().addMouseListener(new MascotEventHandler(this));

    }

    public String toString() {
        return "マスコット" + this.id;
    }

    void tick() {
        if ((isAnimating()) && (getBehavior() != null)) {
            try {
                getBehavior().next();
            } catch (CantBeAliveException e) {
                log.log(Level.SEVERE, "生き続けることが出来ない状況", e);
                dispose();
            }

            setTime(getTime() + 1);
        }
    }

    public void apply() {
        if (animating) {
            if (image != null) {
                window.setPosition(anchor.x - image.getCenter().x, anchor.y - image.getCenter().y);
                if (SoundFactory.sound3D) {
                    voiceController.updatePosition(anchor, environment.getScreen());
                    sfxController.updatePosition(anchor, environment.getScreen());
                }
                window.setImage(image.getImage());
                if (!window.isVisible())
                    window.setVisible(true);

                window.updateWindow();
            } else if (window.isVisible()) {
                window.setVisible(false);
            }
        }
    }

    private boolean disposed = false;

    public boolean isDisposed() {
        return disposed;
    }

    public void dispose() {
        log.log(Level.INFO, "マスコット破棄({0})", this);
        animating = false;
        disposed = true;
        getManager().remove(this);
        voiceController.release();
        sfxController.release();
    }

    public Manager getManager() {
        return this.manager;
    }

    public void setManager(Manager manager) {
        this.manager = manager;
    }

    public Point getAnchor() {
        return this.anchor;
    }

    public void setAnchor(Point anchor) {
        this.anchor = anchor;
    }

    public MascotImage getImage() {
        return this.image;
    }

    public void setImage(MascotImage image) {
        this.image = image;
    }

    public boolean isLookRight() {
        return this.lookRight;
    }

    public void setLookRight(boolean lookRight) {
        this.lookRight = lookRight;
    }

    public Rectangle getBounds() {
        int top = getAnchor().y - getImage().getCenter().y;
        int left = getAnchor().x - getImage().getCenter().x;
        return new Rectangle(left, top, getImage().getSize().width, getImage().getSize().height);
    }

    public int getTime() {
        return this.time;
    }

    private void setTime(int time) {
        this.time = time;
    }

    public Behavior getBehavior() {
        return this.behavior;
    }

    public void setBehavior(Behavior behavior) throws CantBeAliveException {
        this.behavior = behavior;
        this.behavior.init(this);
    }

    private boolean isAnimating() {
        return this.animating;
    }

    void setAnimating(boolean animating) {
        this.animating = animating;
    }

    TranslucentWindow getWindow() {
        return this.window;
    }

    public MascotEnvironment getEnvironment() {
        return this.environment;
    }

    //javascript中要使用，非冗余
    public int getTotalCount() {
        return this.manager.getCount();
    }
}