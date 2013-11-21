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
    private BufferedImage image;
    MacTranslucentWindow(){
        super(WindowUtils.getAlphaCompatibleGraphicsConfiguration());
        this.getRootPane().putClientProperty("Window.shadow",Boolean.FALSE);
        WindowUtils.setWindowTransparent(this,true);
    }
    @Override
    public JWindow asJWindow() {
        return this;
    }

    @Override
    public void setImage(NativeImage image) {
        this.image = ((MacNativeImage)image).getImage();
    }

    @Override
    public void setPosition(int x, int y) {
        //x=100;y=100;
        if(null!=this.image) this.setBounds(x,y,image.getWidth(),image.getHeight());
    }


    @Override
    public void updateWindow() {
        repaint();
    }

    @Override
    public void paint(Graphics graphics) {
        final Rectangle clipBounds = graphics.getClipBounds();
        graphics.clearRect(clipBounds.x,clipBounds.y,clipBounds.width,clipBounds.height);
        graphics.drawImage(this.image, 0, 0, null);
    }
}
