package com.group_finity.mascot;

import com.group_finity.mascot.sound.SoundBuffer;
import com.group_finity.mascot.sound.SoundFactory;

import javax.swing.*;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import java.awt.*;
import java.awt.event.*;
import java.util.ResourceBundle;


class MascotPopupMenu {
    private static boolean showSystemTrayMenu = false;
    private static final ResourceBundle resourceBundle = Main.resourceBundle;

    static final ActionListener restoreMenuListener = new ActionListener() {
        final SoundBuffer voice = SoundFactory.getSound(resourceBundle.getString("sound.cmd_restore_window"));
        final Runnable cmd = new Runnable() {

            final SoundBuffer voiceFalse = SoundFactory.getSound(resourceBundle.getString("sound.response_nothing_reset"));
            final SoundBuffer voiceTrue = SoundFactory.getSound(resourceBundle.getString("sound.response_reset"));

            @Override
            public void run() {
                if (NativeFactory.getInstance().getEnvironment().restoreIE()) {
                    SoundFactory.invokeAfterSound(voiceTrue, null);
                } else {
                    SoundFactory.invokeAfterSound(voiceFalse, null);
                }
            }
        };

        @Override
        public void actionPerformed(ActionEvent e) {
            SoundFactory.invokeAfterSound(voice, cmd);
        }
    };
    static final ActionListener increaseMenuListener = new ActionListener() {
        final SoundBuffer sound = SoundFactory.getSound(resourceBundle.getString("sound.cmd_one_more_mascot"));
        final Runnable cmd = new Runnable() {

            @Override
            public void run() {
                Main.getInstance().createMascot();
            }

        };

        @Override
        public void actionPerformed(ActionEvent e) {
            SoundFactory.invokeAfterSound(sound, cmd);
        }
    };
    static final ActionListener gatherMenuListener = new ActionListener() {
        final SoundBuffer sound = SoundFactory.getSound(resourceBundle.getString("sound.cmd_together"));
        final Runnable cmd = new Runnable() {

            @Override
            public void run() {
                Main.getInstance().gatherAll();
            }
        };

        @Override
        public void actionPerformed(ActionEvent e) {
            SoundFactory.invokeAfterSound(sound, cmd);
        }
    };
    static final ActionListener oneMenuListener = new ActionListener() {
        final SoundBuffer sound = SoundFactory.getSound(resourceBundle.getString("sound.cmd_remain_one_mascot"));
        final Runnable cmd = new Runnable() {

            @Override
            public void run() {
                Main.getInstance().remainOne();
            }
        };

        @Override
        public void actionPerformed(ActionEvent e) {
            SoundFactory.invokeAfterSound(sound, cmd);
        }
    };
    static final ActionListener aboutMenuListener = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
            Main.getInstance().showAbout();
        }
    };
    static final ActionListener closeMenuListener = new ActionListener() {
        final SoundBuffer sound = SoundFactory.getSound(resourceBundle.getString("sound.cmd_exit_app"));
        final Runnable cmd = new Runnable() {
            @Override
            public void run() {
                Main.getInstance().exit();
            }
        };

        @Override
        public void actionPerformed(ActionEvent e) {
            SoundFactory.invokeAfterSound(sound, cmd);
        }
    };

    private static class disposrMenuListener implements ActionListener {
        final static SoundBuffer sound = SoundFactory.getSound(resourceBundle.getString("sound.cmd_dispose_mascot"));
        final Mascot mascot;

        public disposrMenuListener(Mascot mascot) {
            this.mascot = mascot;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            final Runnable cmd = new Runnable() {
                @Override
                public void run() {
                    mascot.dispose();
                }
            };
            SoundFactory.invokeAfterSound(sound, cmd);
        }
    }


    public static JPopupMenu createJPopupMenu(final Mascot mascot) {
        final JPopupMenu popupMenu = new JPopupMenu();
        if (showSystemTrayMenu) {
            prepareMainMenu(popupMenu);
            popupMenu.addSeparator();
        }
        final JMenuItem disposeMenu = new JMenuItem(resourceBundle.getString("menu.cmd_dispose_mascot"));//"ばいばい");
        disposeMenu.addActionListener(new disposrMenuListener(mascot));
        popupMenu.add(disposeMenu);
        mascot.getWindow().asJWindow().add(popupMenu);
        popupMenu.addPopupMenuListener(new PopupMenuListener() {
            @Override
            public void popupMenuCanceled(final PopupMenuEvent e) {
            }

            @Override
            public void popupMenuWillBecomeInvisible(final PopupMenuEvent e) {
                mascot.setAnimating(true);
            }

            @Override
            public void popupMenuWillBecomeVisible(final PopupMenuEvent e) {
                mascot.setAnimating(false);
            }
        });
        return popupMenu;
    }

    public static void prepareTrayIcon(final TrayIcon icon) {
        icon.addMouseListener(new MouseListener() {
            final SoundBuffer sound = SoundFactory.getSound(resourceBundle.getString("sound.cmd_one_more_mascot"));
            final Runnable cmd = new Runnable() {

                @Override
                public void run() {
                    Main.getInstance().createMascot();
                }

            };

            @Override
            public void mouseClicked(MouseEvent e) {
                if (SwingUtilities.isLeftMouseButton(e)) {
                    SoundFactory.invokeAfterSound(sound, cmd);
                }
            }

            @Override
            public void mousePressed(MouseEvent e) {
            }

            @Override
            public void mouseReleased(MouseEvent e) {
            }

            @Override
            public void mouseEntered(MouseEvent e) {
            }

            @Override
            public void mouseExited(MouseEvent e) {
            }
        });

    }

    private static final CheckboxMenuItem voiceMenu = new CheckboxMenuItem(resourceBundle.getString("menu.checkbox_voice"), SoundFactory.voiceOn);
    private static final JCheckBoxMenuItem jVoiceMenu = new JCheckBoxMenuItem(resourceBundle.getString("menu.checkbox_voice"), SoundFactory.voiceOn);

    static {
        ItemListener sfxMenuListener = new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                SoundFactory.voiceOn = e.getStateChange() == ItemEvent.SELECTED;
                voiceMenu.setState(SoundFactory.voiceOn);
                jVoiceMenu.setSelected(SoundFactory.voiceOn);
            }
        };
        voiceMenu.addItemListener(sfxMenuListener);
        jVoiceMenu.addItemListener(sfxMenuListener);
    }

    private static final CheckboxMenuItem sfxMenu = new CheckboxMenuItem(resourceBundle.getString("menu.checkbox_sfx"), SoundFactory.sfxOn);
    private static final JCheckBoxMenuItem jSfxMenu = new JCheckBoxMenuItem(resourceBundle.getString("menu.checkbox_sfx"), SoundFactory.sfxOn);

    static {
        ItemListener sfxMenuListener = new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                SoundFactory.sfxOn = e.getStateChange() == ItemEvent.SELECTED;
                sfxMenu.setState(SoundFactory.sfxOn);
                jSfxMenu.setSelected(SoundFactory.sfxOn);
            }
        };
        sfxMenu.addItemListener(sfxMenuListener);
        jSfxMenu.addItemListener(sfxMenuListener);
    }

    public static void prepareMainMenu(PopupMenu mainMenu) {
        final MenuItem increaseMenu = new MenuItem(resourceBundle.getString("menu.cmd_one_more_mascot"));//増やす");
        increaseMenu.addActionListener(increaseMenuListener);
        final MenuItem gatherMenu = new MenuItem(resourceBundle.getString("menu.cmd_together"));//あつまれ！");
        gatherMenu.addActionListener(gatherMenuListener);
        final MenuItem oneMenu = new MenuItem(resourceBundle.getString("menu.cmd_remain_one_mascot"));//一匹だけ残す");
        oneMenu.addActionListener(oneMenuListener);
        final MenuItem restoreMenu = new MenuItem(resourceBundle.getString("menu.cmd_restore_window"));//IEを元に戻す");
        restoreMenu.addActionListener(restoreMenuListener);
        final MenuItem aboutMenu = new MenuItem(resourceBundle.getString("menu.cmd_show_about_win"));//"Toshimeji について");
        aboutMenu.addActionListener(aboutMenuListener);
        final MenuItem closeMenu = new MenuItem(resourceBundle.getString("menu.cmd_exit_app"));//ばいばい");
        closeMenu.addActionListener(closeMenuListener);
        mainMenu.add(increaseMenu);
        mainMenu.add(gatherMenu);
        mainMenu.add(oneMenu);
        mainMenu.add(restoreMenu);
        mainMenu.addSeparator();
        mainMenu.add(voiceMenu);
        mainMenu.add(sfxMenu);
        mainMenu.addSeparator();
        mainMenu.add(aboutMenu);
        mainMenu.add(closeMenu);
    }

    public static void prepareMainMenu(JPopupMenu mainMenu) {
        final JMenuItem increaseMenu = new JMenuItem(resourceBundle.getString("menu.cmd_one_more_mascot"));//増やす");
        increaseMenu.addActionListener(increaseMenuListener);
        final JMenuItem gatherMenu = new JMenuItem(resourceBundle.getString("menu.cmd_together"));//あつまれ！");
        gatherMenu.addActionListener(gatherMenuListener);
        final JMenuItem oneMenu = new JMenuItem(resourceBundle.getString("menu.cmd_remain_one_mascot"));//一匹だけ残す");
        oneMenu.addActionListener(oneMenuListener);
        final JMenuItem restoreMenu = new JMenuItem(resourceBundle.getString("menu.cmd_restore_window"));//IEを元に戻す");
        restoreMenu.addActionListener(restoreMenuListener);
        final JMenuItem aboutMenu = new JMenuItem(resourceBundle.getString("menu.cmd_show_about_win"));//"Toshimeji について");
        aboutMenu.addActionListener(aboutMenuListener);
        final JMenuItem closeMenu = new JMenuItem(resourceBundle.getString("menu.cmd_exit_app"));//ばいばい");
        closeMenu.addActionListener(closeMenuListener);
        mainMenu.add(increaseMenu);
        mainMenu.add(gatherMenu);
        mainMenu.add(oneMenu);
        mainMenu.add(restoreMenu);
        mainMenu.addSeparator();
        mainMenu.add(jVoiceMenu);
        mainMenu.add(jSfxMenu);
        mainMenu.addSeparator();
        mainMenu.add(aboutMenu);
        mainMenu.add(closeMenu);
    }

    public static void setShowSystemTrayMenu(boolean showSystemTrayMenu) {
        MascotPopupMenu.showSystemTrayMenu = showSystemTrayMenu;
    }
}
