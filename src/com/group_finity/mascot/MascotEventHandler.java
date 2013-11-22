package com.group_finity.mascot;

import com.group_finity.mascot.exception.CantBeAliveException;

import javax.swing.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MascotEventHandler implements MouseListener {

    private static final Logger log = Logger.getLogger(MascotEventHandler.class.getName());

    private final Mascot mascot;
    private JPopupMenu popup;

    public MascotEventHandler(Mascot mascot) {
        this.mascot = mascot;
    }

    public void mousePressed(final MouseEvent event) {
        if (event.isPopupTrigger()){
            getPopup().show(event.getComponent(), event.getX(), event.getY());
        } else {    if (mascot.getBehavior() != null) {
                try {
                    mascot.getBehavior().mousePressed(event);
                    if (SwingUtilities.isLeftMouseButton(event)) mascot.getWindow().asJWindow().setCursor(mascot.getManager().main.cursorPressed);
                } catch (final CantBeAliveException e) {
                    log.log(Level.SEVERE, "生き続けることが出来ない状況", e);
                    mascot.dispose();
                }
            }
        }
    }

    public JPopupMenu getPopup() {
        if (null == popup) {
            popup = mascot.getManager().main.mascotPopupMenu.createJPopupMenu(mascot);
        }
        return popup;
    }

    public void mouseReleased(final MouseEvent event) {
        if (event.isPopupTrigger()){
            getPopup().show(event.getComponent(), event.getX(), event.getY());
        } else {
            if (mascot.getBehavior() != null) {
                try {
                    if (SwingUtilities.isLeftMouseButton(event)) mascot.getWindow().asJWindow().setCursor(mascot.getManager().main.cursor);
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
