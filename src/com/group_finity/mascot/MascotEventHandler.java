package com.group_finity.mascot;

import com.group_finity.mascot.exception.CantBeAliveException;

import javax.swing.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created with IntelliJ IDEA.
 * User: SMagi
 * Date: 13-12-3
 * Time: 下午3:36
 * To change this template use File | Settings | File Templates.
 */
public class MascotEventHandler implements MouseListener {

    private static final Logger log = Logger.getLogger(MascotEventHandler.class.getName());
    private final Mascot mascot;
    public MascotEventHandler(Mascot mascot) {
        this.mascot = mascot;
    }

    public void mousePressed(final MouseEvent event) {
        if (event.isPopupTrigger()) {
            mascot.getManager().main.mascotPopupMenu.showPopupMenu(mascot, event.getComponent(), event.getX(), event.getY());
        } else {
            if (mascot.getBehavior() != null) {
                try {
                    mascot.getBehavior().mousePressed(event);
                    if (SwingUtilities.isLeftMouseButton(event))
                        mascot.getWindow().asJWindow().setCursor(mascot.getManager().main.cursorPressed);
                } catch (final CantBeAliveException e) {
                    log.log(Level.SEVERE, "生き続けることが出来ない状況", e);
                    mascot.dispose();
                }
            }
        }
    }

    public void mouseReleased(final MouseEvent event) {
        if (event.isPopupTrigger()) {
            mascot.getManager().main.mascotPopupMenu.showPopupMenu(mascot, event.getComponent(), event.getX(), event.getY());
        } else {
            if (mascot.getBehavior() != null) {
                try {
                    if (SwingUtilities.isLeftMouseButton(event))
                        mascot.getWindow().asJWindow().setCursor(mascot.getManager().main.cursor);
                    mascot.getBehavior().mouseReleased(event);
                } catch (final CantBeAliveException e) {
                    log.log(Level.SEVERE, "生き続けることが出来ない状況", e);
                    mascot.dispose();
                }
            }
        }

    }

    @Override
    public void mouseClicked(MouseEvent e) {
    }

    @Override
    public void mouseEntered(MouseEvent e) {
    }

    @Override
    public void mouseExited(MouseEvent e) {
    }


}
