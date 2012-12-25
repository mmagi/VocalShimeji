package com.group_finity.mascot;

import com.group_finity.mascot.config.Configuration;
import com.group_finity.mascot.config.Entry;
import com.group_finity.mascot.exception.BehaviorInstantiationException;
import com.group_finity.mascot.exception.CantBeAliveException;
import com.group_finity.mascot.exception.ConfigurationException;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import javax.imageio.ImageIO;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.swing.*;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

public class Main {
    static final Logger log = Logger.getLogger(Main.class.getName());
    static final String BEHAVIOR_GATHER = "マウスの周りに集まる";
    private static Main instance = new Main();
    private static final Manager manager = new Manager();

    private final Configuration configuration = new Configuration();

    public Main() {
    }

    public static Main getInstance() {
        return instance;
    }

    private static final void checkJnaDll() {
        String arc = System.getProperty("os.arch").endsWith("64") ? "x86_64" : "x86";
        String dir = "lib/win32-" + arc;
        File jnalib = new File(new File(dir), System.mapLibraryName("jnidispatch"));
        if (jnalib.exists())
            System.setProperty("jna.boot.library.path", dir);
        else {
            throw new RuntimeException(jnalib.getAbsolutePath() + " is missing.");
        }
    }

    private final static Clip initSound() {
        Clip clip = null;
        // play init sound
        try {
            clip = AudioSystem.getClip();
            AudioInputStream ais = AudioSystem.getAudioInputStream(Main.class.getResource("/media/init.wav"));
            clip.open(ais);
            clip.start();
        } catch (Exception e) {
            log.log(Level.INFO, "载入欢迎语音出错");
        }
        // preload all sound
        log.log(Level.INFO, "正在预读语音文件({0})", "/media");
        SoundManager.preLoadSounds();
        return clip;
    }

    private static final SplashScreen splashScreen = new SplashScreen();
    ;

    public static void main(String[] args) {
        splashScreen.showScreen();
        try {
            checkJnaDll();
            Clip clip = initSound();
            getInstance().run();
            clip.drain();
            clip.close();
        } catch (Throwable e) {
            log.log(Level.WARNING, "启动时出现错误", e);
        }
    }


    public void run() {
        try {
            loadConfiguration();

            try {
                SwingUtilities.invokeAndWait(new Runnable() {
                    @Override
                    public void run() {
                        createTrayIcon();
                    }
                });
            } catch (InterruptedException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            } catch (InvocationTargetException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }
            createMascot();

            getManager().start();
        } catch (Throwable e) {
            log.log(Level.WARNING, "初始化异常", e);
        }

        splashScreen.dispose();
    }

    private void loadConfiguration() {
        try {
            log.log(Level.INFO, "正在载入配置文件({0})", "/Behavior.xml");

            Document actions = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(Main.class.getResourceAsStream("/Behavior.xml"));

            log.log(Level.INFO, "正在载入配置文件({0})", "/Actions.xml");

            getConfiguration().load(new Entry(actions.getDocumentElement()));

            Document behaviors = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(Main.class.getResourceAsStream("/Actions.xml"));

            getConfiguration().load(new Entry(behaviors.getDocumentElement()));

            getConfiguration().validate();
        } catch (IOException e) {
            log.log(Level.SEVERE, "配置文件读取失败", e);
            exit();
        } catch (SAXException e) {
            log.log(Level.SEVERE, "配置文件读取失败", e);
            exit();
        } catch (ParserConfigurationException e) {
            log.log(Level.SEVERE, "配置文件读取失败", e);
            exit();
        } catch (ConfigurationException e) {
            log.log(Level.SEVERE, "配置文件读取失败", e);
            exit();
        }
    }

    MyJPopupMenu mainMenu = new MyJPopupMenu();

    private void createTrayIcon() {
        log.log(Level.INFO, "正在炼成蛋黄酱瓶子");
        if (SystemTray.getSystemTray() == null) {
            return;
        }
        MyJPopupMenu.prepareMainMenu(mainMenu);
        try {
            TrayIcon icon = new TrayIcon(ImageIO.read(Main.class.getResource("/icon.png")), "十四めじ", null);
            MyJPopupMenu.prepareTrayIcon(icon, mainMenu);
            SystemTray.getSystemTray().add(icon);
        } catch (IOException e) {
            log.log(Level.SEVERE, "トレイアイコンの作成に失敗", e);
            exit();
        } catch (AWTException e) {
            log.log(Level.SEVERE, "トレイアイコンの作成に失敗", e);
            MascotEventHandler.setShowSystemTrayMenu(true);
            getManager().setExitOnLastRemoved(true);
        }
    }

    AboutScreen aboutScreen = new AboutScreen();

    protected void showAbout() {
        aboutScreen.showScreen();
    }

    public void createMascot() {

        Mascot mascot = new Mascot();

        mascot.setAnchor(new Point(-1000, -1000));

        mascot.setLookRight(Math.random() < 0.5D);
        try {
            mascot.setBehavior(getConfiguration().buildBehavior(null, mascot));

            getManager().add(mascot);
        } catch (BehaviorInstantiationException e) {
            log.log(Level.SEVERE, "最初の行動の初期化に失敗しました", e);
            mascot.dispose();
        } catch (CantBeAliveException e) {
            log.log(Level.SEVERE, "生き続けることが出来ない状況", e);
            mascot.dispose();
        }
    }

    public void gatherAll() {
        getManager().setBehaviorAll(getConfiguration(), BEHAVIOR_GATHER);
    }

    public void remainOne() {
        getManager().remainOne();
    }

    public void exit() {
        getManager().disposeAll();
        getManager().stop();

        System.exit(0);
    }

    public Configuration getConfiguration() {
        return this.configuration;
    }

    private Manager getManager() {
        return this.manager;
    }

    static {
        try {
            LogManager.getLogManager().readConfiguration(Main.class.getResourceAsStream("/logging.properties"));
        } catch (SecurityException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}