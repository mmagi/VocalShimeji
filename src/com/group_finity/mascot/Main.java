package com.group_finity.mascot;

import com.group_finity.mascot.config.Configuration;
import com.group_finity.mascot.config.Entry;
import com.group_finity.mascot.exception.BehaviorInstantiationException;
import com.group_finity.mascot.exception.CantBeAliveException;
import com.group_finity.mascot.exception.ConfigurationException;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.awt.*;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

public class Main {
    static final Logger log = Logger.getLogger(Main.class.getName());
    static final String BEHAVIOR_GATHER = "マウスの周りに集まる";
    private static Main instance = new Main();

    public static Main getInstance() {
        return instance;
    }

    private final Manager manager;

    private final Configuration configuration;

    private Main() {
        manager = new Manager();
        configuration = new Configuration();
    }


    public void run() {
            loadConfiguration();

            try {
                SwingUtilities.invokeAndWait(new Runnable() {
                    @Override
                    public void run() {
                        createTrayIcon();
                    }
                });
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                throw new RuntimeException("创建托盘图标出错",e);
            }
            createMascot();

            getManager().start();

    }

    private void loadConfiguration() {
        try {
            log.log(Level.INFO, "正在载入配置文件({0})", "Behavior.xml");

            Document actions = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(Main.class.getResourceAsStream("/conf/Behavior.xml"));

            log.log(Level.INFO, "正在载入配置文件({0})", "Actions.xml");

            getConfiguration().load(new Entry(actions.getDocumentElement()));

            Document behaviors = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(Main.class.getResourceAsStream("/conf/Actions.xml"));

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
        log.log(Level.INFO, "正在创建托盘图标");
        if (SystemTray.getSystemTray() == null) {
            return;
        }
        MyJPopupMenu.prepareMainMenu(mainMenu);
        try {
            TrayIcon icon = new TrayIcon(ImageIO.read(Main.class.getResource("/icon.png")), "VocalShimeji", null);
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

    JWindow aboutScreen;
    public void setAboutScreen(JWindow aboutScreen){
        this.aboutScreen=aboutScreen;
    }
    protected void showAbout() {
        if(null!=aboutScreen)
            aboutScreen.setVisible(true);
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
            LogManager.getLogManager().readConfiguration(Main.class.getResourceAsStream("/conf/logging.properties"));
        } catch (SecurityException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}