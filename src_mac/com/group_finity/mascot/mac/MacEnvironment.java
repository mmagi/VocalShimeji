package com.group_finity.mascot.mac;

import com.group_finity.mascot.environment.Area;
import com.group_finity.mascot.environment.Environment;

import java.awt.*;

/**
 * Created with IntelliJ IDEA.
 * User: magi
 * Date: 20/11/13
 * Time: 8:57 PM
 * To change this template use File | Settings | File Templates.
 */
public class MacEnvironment extends Environment {
    @Override
    protected Area getWorkArea() {
        return getScreen();
    }

    @Override
    public Area getActiveIE() {
        return getScreen();  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void moveActiveIE(Point point) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public boolean restoreIE() {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }
}
