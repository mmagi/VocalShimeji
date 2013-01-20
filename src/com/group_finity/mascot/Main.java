package com.group_finity.mascot;

import com.group_finity.mascot.config.Configuration;
import com.group_finity.mascot.config.Entry;
import com.group_finity.mascot.exception.BehaviorInstantiationException;
import com.group_finity.mascot.exception.CantBeAliveException;
import com.group_finity.mascot.exception.ConfigurationException;
import com.group_finity.mascot.sound.SoundFactory;
import com.group_finity.mascot.util.PropertiseBundle;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Main {
    static final Logger log = Logger.getLogger(Main.class.getName());
    static final String BEHAVIOR_GATHER = PropertiseBundle.resourceBundle.getString("action.BEHAVIOR_GATHER");
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

    Cursor cursor;

    private void initSound() {
        try {
            SoundFactory.invokeAfterSound(SoundFactory.getSound(PropertiseBundle.resourceBundle.getString("sound.init")), new Runnable() {
                @Override
                public void run() {
                }
            });
        } catch (final Exception e) {
            Main.log.log(Level.WARNING, PropertiseBundle.resourceBundle.getString("exception.init_sound_failed"));
        }
    }

    public void run() {
        initSound();
        loadConfiguration();
        BufferedImage cursorImage = null;
        try {
            cursorImage = ImageIO.read(Main.class.getResource(PropertiseBundle.resourceBundle.getString("image.mouse_icon")));
            Dimension size = Toolkit.getDefaultToolkit().getBestCursorSize(cursorImage.getWidth(), cursorImage.getHeight());
            BufferedImage scaledImage = new BufferedImage(size.width, size.height, cursorImage.getType());

            // Paint scaled version of image to new image

            Graphics2D graphics2D = scaledImage.createGraphics();
            graphics2D.drawImage(cursorImage, 0, 0, cursorImage.getWidth(), cursorImage.getHeight(), null);
            cursorImage = scaledImage;
            // clean up

            graphics2D.dispose();
        } catch (IOException e) {
            e.printStackTrace();
        }
        Point hotSpot = new Point(8, 3);
        String cursorName = "VocalShimejiCursor";
        cursor = Toolkit.getDefaultToolkit().createCustomCursor(cursorImage, hotSpot, cursorName);
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
            log.log(Level.WARNING, PropertiseBundle.resourceBundle.getString("exception.init_tray_icon_failed"), e);
        }
        createMascot();

        getManager().start();

    }

    private void loadConfiguration() {
        try {
            log.log(Level.INFO, PropertiseBundle.resourceBundle.getString("message.loading_config"), "Behavior.xml");

            Document actions = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(Main.class.getResourceAsStream("/conf/Behavior.xml"));

            log.log(Level.INFO, PropertiseBundle.resourceBundle.getString("message.loading_config"), "Actions.xml");

            getConfiguration().load(new Entry(actions.getDocumentElement()));

            Document behaviors = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(Main.class.getResourceAsStream("/conf/Actions.xml"));

            getConfiguration().load(new Entry(behaviors.getDocumentElement()));

            getConfiguration().validate();
        } catch (IOException e) {
            log.log(Level.SEVERE, PropertiseBundle.resourceBundle.getString("exception.load_config_failed"), e);
            exit();
        } catch (SAXException e) {
            log.log(Level.SEVERE, PropertiseBundle.resourceBundle.getString("exception.load_config_failed"), e);
            exit();
        } catch (ParserConfigurationException e) {
            log.log(Level.SEVERE, PropertiseBundle.resourceBundle.getString("exception.load_config_failed"), e);
            exit();
        } catch (ConfigurationException e) {
            log.log(Level.SEVERE, PropertiseBundle.resourceBundle.getString("exception.load_config_failed"), e);
            exit();
        }
    }

    PopupMenu mainMenu = new PopupMenu();

    private void createTrayIcon() {
        log.log(Level.INFO, PropertiseBundle.resourceBundle.getString("message.create_tray_icon"));
        if (SystemTray.getSystemTray() == null) {
            return;
        }
        MascotPopupMenu.prepareMainMenu(mainMenu);
        try {
            TrayIcon icon = new TrayIcon(ImageIO.read(Main.class.getResource(PropertiseBundle.resourceBundle.getString("image.tray_icon"))), "VocalShimeji", mainMenu);
            MascotPopupMenu.prepareTrayIcon(icon);
            SystemTray.getSystemTray().add(icon);
        } catch (IOException e) {
            log.log(Level.SEVERE, PropertiseBundle.resourceBundle.getString("exception.create_tray_icon_failed"), e);
            exit();
        } catch (AWTException e) {
            log.log(Level.SEVERE, PropertiseBundle.resourceBundle.getString("exception.create_tray_icon_failed"), e);
            MascotPopupMenu.setShowSystemTrayMenu(true);
            getManager().setExitOnLastRemoved(true);
        }
    }

    JWindow aboutScreen;

    public void setAboutScreen(JWindow aboutScreen) {
        this.aboutScreen = aboutScreen;
    }

    protected void showAbout() {
        if (null != aboutScreen)
            aboutScreen.setVisible(true);
    }

    public void createMascot() {

        Mascot mascot = new Mascot();

        mascot.setAnchor(new Point(-1000, -1000));
        mascot.getWindow().asJWindow().setCursor(cursor);
        mascot.setLookRight(Math.random() < 0.5D);
        try {
            mascot.setBehavior(getConfiguration().buildBehavior(null, mascot));
            getManager().add(mascot);
            System.out.println(getManager().getCount());
        } catch (BehaviorInstantiationException e) {
            log.log(Level.SEVERE, PropertiseBundle.resourceBundle.getString("exception.mascot.build_behavior_failed"), e);
            mascot.dispose();
        } catch (CantBeAliveException e) {
            log.log(Level.SEVERE, PropertiseBundle.resourceBundle.getString("exception.mascot.apply_behavior_failed"), e);
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
        System.exit(0);
    }

    public Configuration getConfiguration() {
        return this.configuration;
    }

    private Manager getManager() {
        return this.manager;
    }

}