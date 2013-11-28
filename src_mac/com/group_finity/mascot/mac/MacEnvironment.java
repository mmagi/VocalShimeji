package com.group_finity.mascot.mac;

import com.group_finity.mascot.environment.Area;
import com.group_finity.mascot.environment.Environment;
import com.sun.jna.Native;
import com.sun.jna.NativeLibrary;

import java.awt.*;


/**
 * Created with IntelliJ IDEA.
 * User: magi
 * Date: 20/11/13
 * Time: 8:57 PM
 * To change this template use File | Settings | File Templates.
 */
public class MacEnvironment extends Environment {
    final private static Area desktopArea = new Area();
    final private static Area safariArea = new Area();

    static {
        NativeLibrary.addSearchPath("vocalshimejibridge", "lib/macosx/");
        Native.register("vocalshimejibridge");
        initShimejiBridge();
    }

    public static native int initShimejiBridge();
    public static native int resetSafariPos();
    public static native int getSafariArea(int area []);
    public static native int getDesktopArea(int area []);
    public static native int moveSafariTo(final int x,final int y,int area[]);
    @Override
    protected Area getWorkArea() {
        //return getScreen();
        return desktopArea;
    }

    @Override
    public Area getActiveIE() {
        return safariArea;
    }

    private static final Rectangle safariR = new Rectangle();
    private static final Rectangle desktopR = new Rectangle();
    private static final int safariA [] = new int [4];
    private static final int desktopA [] = new int [4];
    @Override
    public void moveActiveIE(Point point) {
        if(moveSafariTo(point.x, point.y, safariA)==1){
            safariR.setBounds(safariA[0],safariA[1],safariA[2],safariA[3]);
            safariArea.set(safariR);
        }else {
            safariArea.setVisible(false);
        }
    }

    @Override
    public boolean restoreIE() {
       return resetSafariPos()==1;
    }
    @Override
    public void tick() {
        super.tick();
        if(getDesktopArea(desktopA)==1){
            desktopR.setBounds(desktopA[0],desktopA[1],desktopA[2],desktopA[3]);
            desktopArea.set(desktopR);
        }
        if(getSafariArea(safariA)==1){
            safariR.setBounds(safariA[0],safariA[1],safariA[2],safariA[3]);
            safariArea.set(safariR);
            safariArea.setVisible(true);
        }else {
            safariArea.setVisible(false);
        }

    }
}
