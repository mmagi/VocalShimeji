package com.group_finity.mascot;

import com.group_finity.mascot.sound.Sound;
import com.group_finity.mascot.sound.SoundFactory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;


class MascotPopupMenu {

    static final MenuItem increaseMenu = new MenuItem("增加一只");//増やす");
    static final MenuItem gatherMenu = new MenuItem("集合！");//あつまれ！");
    static final MenuItem oneMenu = new MenuItem("只留一只");//一匹だけ残す");
    static final MenuItem restoreMenu = new MenuItem("还原浏览器窗口");//IEを元に戻す");;
    static final CheckboxMenuItem voiceMenu = new CheckboxMenuItem("语音", true);
    static final CheckboxMenuItem sfxMenu = new CheckboxMenuItem("音效", true);
    static final MenuItem aboutMenu = new MenuItem("关于VocalShimeji");//"Toshimeji について");
    static final MenuItem closeMenu = new MenuItem("再见～");//ばいばい");
    /**
     * トレイアイコンの分のメニューも表示するかどうか.
     * トレイアイコンの作成に失敗した時はここでトレイアイコンの分のメニューも表示する必要がある.
     */
    private static boolean showSystemTrayMenu = false;

    static {
        restoreMenu.addActionListener(new ActionListener() {
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
        });
        increaseMenu.addActionListener(new ActionListener() {
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
        });
        gatherMenu.addActionListener(new ActionListener() {
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
        });
        oneMenu.addActionListener(new ActionListener() {
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
        });
        voiceMenu.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                SoundFactory.voiceOn = voiceMenu.getState();
            }
        });
        sfxMenu.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                SoundFactory.sfxOn = sfxMenu.getState();
            }
        });
        aboutMenu.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Main.getInstance().showAbout();
            }
        });
        closeMenu.addActionListener(new ActionListener() {
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
        });
    }

    public static JMenuItem createDisposeMenu(final Mascot mascot) {
        final JMenuItem disposeMenu = new JMenuItem("再见吧");//"ばいばい");
        disposeMenu.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                final Sound sound = SoundFactory.getSound("/cmd_hide.wav");
                final Runnable cmd = new Runnable() {
                    @Override
                    public void run() {
                        mascot.dispose();
                    }
                };
                SoundFactory.invokeAfterSound(sound, cmd);
            }
        });
        return disposeMenu;
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

    public static void prepareMainMenu(PopupMenu mainMenu) {
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

    public static void setShowSystemTrayMenu(boolean showSystemTrayMenu) {
        showSystemTrayMenu = showSystemTrayMenu;
    }

    public static boolean isShowSystemTrayMenu() {
        return showSystemTrayMenu;
    }
}
