package com.group_finity.mascot.mac;

import com.group_finity.mascot.environment.Area;
import com.group_finity.mascot.environment.Environment;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import java.awt.*;
import java.util.ArrayList;

/**
 * Created with IntelliJ IDEA.
 * User: magi
 * Date: 20/11/13
 * Time: 8:57 PM
 * To change this template use File | Settings | File Templates.
 */
public class MacEnvironment extends Environment {
    private final static Area workArea = new Area();

    final public static Area activeIE = new Area();
    final public static ScriptEngine appleScript = new ScriptEngineManager().getEngineByName("AppleScript");;
    @Override
    protected Area getWorkArea() {
        return getScreen();
    }

    @Override
    public Area getActiveIE() {
        return activeIE;
    }

    @Override
    public void moveActiveIE(Point point) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public boolean restoreIE() {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }
    private static Rectangle getActiveRect(){
        try {
            Object eval = appleScript.eval("try\n" +
                    "\ttell application (path to frontmost application as text) to get the bounds of the front window\n" +
                    "on error msg\n" +
                    "\tnull\n" +
                    "end try");
            if ((null != eval) && (eval instanceof ArrayList)){//ArrayList(Long)
                ArrayList<Long> list = (ArrayList) eval;
                return new Rectangle(list.get(0).intValue(),list.get(1).intValue(),(int)(list.get(2)-list.get(0)),(int)(list.get(3)-list.get(1)));
            }
        } catch (Exception e) {
        }
        return null;
    }
    @Override
    public void tick() {
        super.tick();
        final Rectangle rect = getActiveRect();
        if (null != rect) {activeIE.setVisible(rect.intersects(getScreen().toRectangle()));activeIE.set(rect);}
    }
}
