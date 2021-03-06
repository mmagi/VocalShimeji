package com.group_finity.mascot.mac;

import com.group_finity.mascot.image.NativeImage;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

/**
 * Created with IntelliJ IDEA.
 * User: magi
 * Date: 20/11/13
 * Time: 9:00 PM
 * To change this template use File | Settings | File Templates.
 */
public class MacNativeImage implements NativeImage {
    final BufferedImage image;
    public MacNativeImage(BufferedImage src) {
        image=src;
    }

    public BufferedImage getImage() {
        return this.image;
    }
    public boolean difsize(MacNativeImage image){
        return null == image || image.image.getWidth() != this.image.getWidth() || image.image.getHeight() != this.image.getHeight();
    }
}
