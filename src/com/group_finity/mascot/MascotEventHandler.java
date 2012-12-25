package com.group_finity.mascot;

import com.group_finity.mascot.exception.CantBeAliveException;

import javax.swing.*;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MascotEventHandler implements MouseListener {

    private static final Logger log = Logger.getLogger(MascotEventHandler.class.getName());

    /**
     * トレイアイコンの分のメニューも表示するかどうか.
     * トレイアイコンの作成に失敗した時はここでトレイアイコンの分のメニューも表示する必要がある.
     */
    private static boolean showSystemTrayMenu = false;

    public static void setShowSystemTrayMenu(boolean showSystemTrayMenu) {
        MascotEventHandler.showSystemTrayMenu = showSystemTrayMenu;
    }

    public static boolean isShowSystemTrayMenu() {
        return showSystemTrayMenu;
    }

    private final Mascot mascot;
    private final JPopupMenu popup;

    public MascotEventHandler(Mascot mascot) {
        this.mascot = mascot;
        popup = new JPopupMenu();
        popup.add(MyJPopupMenu.createDisposeMenu(mascot));

        if (MascotEventHandler.isShowSystemTrayMenu()) {

            popup.add(new JSeparator());

            MyJPopupMenu.prepareMainMenu(popup);

        }
        popup.addPopupMenuListener(new PopupMenuListener() {
            @Override
            public void popupMenuCanceled(final PopupMenuEvent e) {
            }

            @Override
            public void popupMenuWillBecomeInvisible(final PopupMenuEvent e) {
                getMascot().setAnimating(true);
            }

            @Override
            public void popupMenuWillBecomeVisible(final PopupMenuEvent e) {
                getMascot().setAnimating(false);
            }
        });
    }

    public void mousePressed(final MouseEvent event) {

        // マウスが押されたらドラッグアニメーションに切り替える
        if (getMascot().getBehavior() != null) {
            try {
                getMascot().getBehavior().mousePressed(event);
            } catch (final CantBeAliveException e) {
                log.log(Level.SEVERE, "生き続けることが出来ない状況", e);
                getMascot().dispose();
            }
        }

    }

    public void mouseReleased(final MouseEvent event) {

        if (event.isPopupTrigger()) {
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    popup.show(mascot.getWindow().asJWindow(), event.getX(), event.getY());
                }
            });
        } else {
            if (getMascot().getBehavior() != null) {
                try {
                    getMascot().getBehavior().mouseReleased(event);
                } catch (final CantBeAliveException e) {
                    log.log(Level.SEVERE, "生き続けることが出来ない状況", e);
                    getMascot().dispose();
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

    private Mascot getMascot() {
        return mascot;
    }

}
