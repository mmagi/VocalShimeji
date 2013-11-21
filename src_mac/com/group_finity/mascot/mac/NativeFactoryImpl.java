package com.group_finity.mascot.mac;

import com.group_finity.mascot.NativeFactory;
import com.group_finity.mascot.environment.Environment;
import com.group_finity.mascot.image.NativeImage;
import com.group_finity.mascot.image.TranslucentWindow;

import java.awt.image.BufferedImage;

/**
 * Created with IntelliJ IDEA.
 * User: magi
 * Date: 20/11/13
 * Time: 8:54 PM
 * To change this template use File | Settings | File Templates.
 */
public class NativeFactoryImpl extends NativeFactory {
    private final Environment environment = new MacEnvironment();
    @Override
    public Environment getEnvironment() {
        return this.environment;
    }

    @Override
    public NativeImage newNativeImage(BufferedImage src) {
        return new MacNativeImage(src);  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public TranslucentWindow newTransparentWindow() {
        return new MacTranslucentWindow();  //To change body of implemented methods use File | Settings | File Templates.
    }
}
