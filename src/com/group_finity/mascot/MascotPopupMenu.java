package com.group_finity.mascot;

import com.group_finity.mascot.sound.Sound;
import com.group_finity.mascot.sound.SoundFactory;

import javax.swing.*;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;


class MascotPopupMenu {

    private static boolean showSystemTrayMenu = false;

    static final ActionListener restoreMenuListener = new ActionListener() {
        final Sound voice = SoundFactory.getSound("/cmd_iechange.wav");
        final Runnable cmd = new Runnable() {

            final Sound voiceFalse = SoundFactory.getSound("/response-noIE.wav");
            final Sound voiceTrue = SoundFactory.getSound("/response-resetIE.wav");

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
        final Sound sound = SoundFactory.getSound("/cmd_onemore14.wav");
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
        final Sound sound = SoundFactory.getSound("/cmd_gether.wav");
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
        final Sound sound = SoundFactory.getSound("/cmd_onlyone14.wav");
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
        final Sound sound = SoundFactory.getSound("/cmd_bye.wav");
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
        final static Sound sound = SoundFactory.getSound("/cmd_hide.wav");
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
        final JMenuItem disposeMenu = new JMenuItem("再见吧");//"ばいばい");
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
            final Sound sound = SoundFactory.getSound("/cmd_onemore14.wav");
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

    private static final CheckboxMenuItem voiceMenu = new CheckboxMenuItem("语音", SoundFactory.voiceOn);
    private static final JCheckBoxMenuItem jVoiceMenu = new JCheckBoxMenuItem("语音", SoundFactory.voiceOn);

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

    private static final CheckboxMenuItem sfxMenu = new CheckboxMenuItem("音效", SoundFactory.sfxOn);
    private static final JCheckBoxMenuItem jSfxMenu = new JCheckBoxMenuItem("音效", SoundFactory.sfxOn);

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
        final MenuItem increaseMenu = new MenuItem("增加一只");//増やす");
        increaseMenu.addActionListener(increaseMenuListener);
        final MenuItem gatherMenu = new MenuItem("集合！");//あつまれ！");
        gatherMenu.addActionListener(gatherMenuListener);
        final MenuItem oneMenu = new MenuItem("只留一只");//一匹だけ残す");
        oneMenu.addActionListener(oneMenuListener);
        final MenuItem restoreMenu = new MenuItem("还原浏览器窗口");//IEを元に戻す");
        restoreMenu.addActionListener(restoreMenuListener);
        final MenuItem aboutMenu = new MenuItem("关于VocalShimeji");//"Toshimeji について");
        aboutMenu.addActionListener(aboutMenuListener);
        final MenuItem closeMenu = new MenuItem("再见～");//ばいばい");
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
        final JMenuItem increaseMenu = new JMenuItem("增加一只");//増やす");
        increaseMenu.addActionListener(increaseMenuListener);
        final JMenuItem gatherMenu = new JMenuItem("集合！");//あつまれ！");
        gatherMenu.addActionListener(gatherMenuListener);
        final JMenuItem oneMenu = new JMenuItem("只留一只");//一匹だけ残す");
        oneMenu.addActionListener(oneMenuListener);
        final JMenuItem restoreMenu = new JMenuItem("还原浏览器窗口");//IEを元に戻す");
        restoreMenu.addActionListener(restoreMenuListener);
        final JMenuItem aboutMenu = new JMenuItem("关于VocalShimeji");//"Toshimeji について");
        aboutMenu.addActionListener(aboutMenuListener);
        final JMenuItem closeMenu = new JMenuItem("再见～");//ばいばい");
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
