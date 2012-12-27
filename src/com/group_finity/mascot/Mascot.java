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
    private static final long serialVersionUID = 1L;
    private static final Logger log = Logger.getLogger(Mascot.class.getName());

    private static AtomicInteger lastId = new AtomicInteger();
    private final int id;
    private final TranslucentWindow window = NativeFactory.getInstance().newTransparentWindow();

    private Manager manager = null;

    private Point anchor = new Point(0, 0);

    private MascotImage image = null;

    private boolean lookRight = false;

    private Behavior behavior = null;

    private int time = 0;

    private boolean animating = true;

    private MascotEnvironment environment = new MascotEnvironment(this);

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

    private boolean gintama = false;
    private int gintamaX = 0, gintamaY = 0;

    protected final void setGintama(int x, int y) {
        gintama = true;
        gintamaX = x;
        gintamaY = y;
    }

    protected final void clearGintama() {
        gintama = false;
    }

    private final void gintamaTick() {
        if (gintama) {
            anchor.move((anchor.x * 3 + gintamaX) / 4, (anchor.y * 3 + gintamaY) / 4);
        }
    }

    void tick() {
        if ((isAnimating()) && (getBehavior() != null)) {
            try {
                gintamaTick();
                getBehavior().next();
            } catch (CantBeAliveException e) {
                log.log(Level.SEVERE, "生き続けることが出来ない状況", e);
                dispose();
            }

            setTime(getTime() + 1);
        }
    }

    public void apply() {
        if (isAnimating()) {
            if (getImage() != null) {
                getWindow().asJWindow().setBounds(getBounds());

                getWindow().setImage(getImage().getImage());

                if (!getWindow().asJWindow().isVisible()) {
                    getWindow().asJWindow().setVisible(true);
                }

                getWindow().updateImage();
            } else if (getWindow().asJWindow().isVisible()) {
                getWindow().asJWindow().setVisible(false);
            }
        }
    }

    public void dispose() {
        log.log(Level.INFO, "マスコット破棄({0})", this);

        getWindow().asJWindow().dispose();
        if (getManager() != null)
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

        Rectangle result = new Rectangle(left, top, getImage().getSize().width, getImage().getSize().height);

        return result;
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

    public int getTotalCount() {
        return getManager().getCount();
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

}