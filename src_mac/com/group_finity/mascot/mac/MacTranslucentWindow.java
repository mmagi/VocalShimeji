package com.group_finity.mascot.mac;

import com.group_finity.mascot.image.NativeImage;
import com.group_finity.mascot.image.TranslucentWindow;
import com.sun.jna.platform.WindowUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

/**
 * Created with IntelliJ IDEA.
 * User: magi
 * Date: 20/11/13
 * Time: 8:59 PM
 * To change this template use File | Settings | File Templates.
 */
public class MacTranslucentWindow extends JWindow implements TranslucentWindow {
    private volatile MacNativeImage image;
    private volatile int x, y;
    private volatile boolean imageoutOfDate = false, posoutOfDate = false;

    MacTranslucentWindow() {
        super(WindowUtils.getAlphaCompatibleGraphicsConfiguration());
        this.getRootPane().putClientProperty("Window.shadow", Boolean.FALSE);
        WindowUtils.setWindowTransparent(this, true);
    }

    @Override
    public JWindow asJWindow() {
        return this;
    }

    @Override
    public void setImage(NativeImage image) {
        if (image != this.image && image instanceof MacNativeImage) {
            if (((MacNativeImage) image).difsize(this.image)) posoutOfDate = true;
            this.image = (MacNativeImage) image;
            imageoutOfDate = true;
        }
    }

    @Override
    public void setPosition(int x, int y) {
        if (this.x != x || this.y != y) {
            this.x = x;
            this.y = y;
            posoutOfDate = true;
        }
    }

    @Override
    public void updateWindow() {
        if (posoutOfDate) {
            this.setBounds(x, y, image.image.getWidth(), image.image.getHeight());
            posoutOfDate = false;
        }
        if (imageoutOfDate) {
            final Graphics gg = getGraphics();
            if (gg instanceof Graphics2D) {
                gg.clearRect(0, 0, getWidth(), getHeight());
                gg.drawImage(image.image, 0, 0, null);
            }
            imageoutOfDate = false;
        }
    }

    @Override
    public void paint(Graphics gg) {
    }
}
