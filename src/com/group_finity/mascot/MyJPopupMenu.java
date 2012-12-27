package com.group_finity.mascot;

import com.group_finity.mascot.sound.Sound;
import com.group_finity.mascot.sound.SoundFactory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

// 不用设置托盘图标的popmenu以免弹出菜单时阻塞窗口线程，
// 绑定一个1x1的dialog窗口用来绑定菜单失去焦点事件，解决JPopupMenu无法获取焦点事件
class MyJPopupMenu extends JPopupMenu {
    private final JDialog focusFrame;

    MyJPopupMenu() {
        super();
        focusFrame = new JDialog();
        focusFrame.setUndecorated(true);
        focusFrame.setSize(1, 1);
        focusFrame.addWindowFocusListener(new WindowFocusListener() {

            @Override
            public void windowGainedFocus(WindowEvent e) {
                //NOP
            }

            @Override
            public void windowLostFocus(WindowEvent e) {
                MyJPopupMenu.this.setVisible(false);
            }
        });
        super.setInvoker(focusFrame);
    }


    @Override
    public void setVisible(boolean b) {
        focusFrame.setVisible(b);
        super.setVisible(b);
    }

    @Override
    public void setInvoker(Component invoker) {
        //NOP
    }


    static final JMenuItem increaseMenu = new JMenuItem("增加一只");//増やす");
    static final JMenuItem gatherMenu = new JMenuItem("集合！");//あつまれ！");
    static final JMenuItem oneMenu = new JMenuItem("只留一只");//一匹だけ残す");
    static final JMenuItem restoreMenu = new JMenuItem("还原浏览器窗口");//IEを元に戻す");;
    static final JCheckBoxMenuItem voiceMenu = new JCheckBoxMenuItem("语音", true);
    static final JCheckBoxMenuItem sfxMenu = new JCheckBoxMenuItem("音效", true);
    static final JMenuItem aboutMenu = new JMenuItem("关于Toshimeji");//"Toshimeji について");
    static final JMenuItem closeMenu = new JMenuItem("再见～");//ばいばい");

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

    public static void prepareTrayIcon(final TrayIcon icon, final JPopupMenu menu) {
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
                } else if (SwingUtilities.isRightMouseButton(e)) {
                    menu.setLocation(e.getX(), e.getY());
                    menu.setVisible(true);
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

    public static void prepareMainMenu(JPopupMenu mainMenu) {

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
}
