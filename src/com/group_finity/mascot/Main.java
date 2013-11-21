package com.group_finity.mascot;

import com.group_finity.mascot.config.Configuration;
import com.group_finity.mascot.config.Entry;
import com.group_finity.mascot.exception.ConfigurationException;
import com.group_finity.mascot.image.ImagePairLoader;
import com.group_finity.mascot.sound.SoundFactory;
import com.jogamp.common.jvm.JNILibLoaderBase;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Locale;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

public abstract class Main {

    static final Logger log = Logger.getLogger(Main.class.getName());


    public final ResourceBundle resourceBundle;
    final String BEHAVIOR_GATHER;
    final Configuration configuration;
    final Manager manager;
    final PopupMenu mainMenu;
    final SoundFactory soundFactory;
    final ImagePairLoader imagePairLoader;
    Cursor cursor,cursorPressed;
    final MascotPopupMenu mascotPopupMenu;
    final UserConfig userConfig;

    protected Main(ResourceBundle resourceBundle) {
        this.resourceBundle = resourceBundle;
        mainMenu = new PopupMenu();
        manager = new Manager(this);
        configuration = new Configuration(this);
        soundFactory = new SoundFactory(this);
        imagePairLoader = new ImagePairLoader(this);
        mascotPopupMenu = new MascotPopupMenu(this);
        userConfig = ReadUserConfig(resourceBundle);
        userConfig.onConfigUpdated.add(new UserConfig.Callback() {
            @Override
            public void onConfigUpdated(final UserConfig config) {
                applyUserConfig(config);
            }
        });
        BEHAVIOR_GATHER = resourceBundle.getString("action.BEHAVIOR_GATHER");
    }

    protected static UserConfig ReadUserConfig(final ResourceBundle resourceBundle) {
        UserConfig config = null;
        JAXBContext context=null;
            try {
                context = JAXBContext.newInstance(UserConfig.class);
                final Unmarshaller unmarshaller = context.createUnmarshaller();
                final Object unmarshal = unmarshaller.unmarshal(new File(resourceBundle.getString("configDlg.conf_file_path")));
                if (unmarshal instanceof UserConfig) {
                    config = (UserConfig) unmarshal;
                }
            } catch (JAXBException e) {
                log.log(Level.INFO, resourceBundle.getString("message.using_default_userconfig"));
            }
        if (null == config) config = new UserConfig();
        if (null != context){
            final JAXBContext cxt = context;
            config.onConfigUpdated.add(new UserConfig.Callback() {
                @Override
                public void onConfigUpdated(final UserConfig config) throws Exception {
                    final Marshaller marshaller = cxt.createMarshaller();
                    marshaller.marshal(config, new File(resourceBundle.getString("configDlg.conf_file_path")));
                }
            });
        }
        return config;
    }

    private static void checkJnaDll(String dir) {
        File jnaLib = new File(new File(dir), System.mapLibraryName("jnidispatch"));
        if (jnaLib.exists()) {
            System.setProperty("jna.boot.library.path", dir);
        } else {
            throw new RuntimeException(jnaLib.getAbsolutePath() + "文件未找到.");
        }
    }

    private static void checkJoalLib(String dir) {
        System.setProperty("jogamp.gluegen.UseTempJarCache", "false");
        JNILibLoaderBase.addLoaded("gluegen-rt");
        System.load(new File(dir + "/libgluegen-rt.jnilib").getAbsolutePath());
        JNILibLoaderBase.addLoaded("joal");
        System.load(new File(dir + "/libjoal.jnilib").getAbsolutePath());
        JNILibLoaderBase.addLoaded("OpenAL32");
        System.load(new File(dir + "/libopenal.dylib").getAbsolutePath());
    }

    private static void checkJoalDll(String dir) {
        System.setProperty("jogamp.gluegen.UseTempJarCache", "false");
        JNILibLoaderBase.addLoaded("gluegen-rt");
        System.load(new File(dir + "/gluegen-rt.dll").getAbsolutePath());
        JNILibLoaderBase.addLoaded("joal");
        System.load(new File(dir + "/joal.dll").getAbsolutePath());
        JNILibLoaderBase.addLoaded("OpenAL32");
        System.load(new File(dir + "/OpenAL32.dll").getAbsolutePath());
        JNILibLoaderBase.addLoaded("wrap_oal");
        System.load(new File(dir + "/wrap_oal.dll").getAbsolutePath());
    }
    public static void LoadDlls() {
        String dllPath = null;
        String osname = System.getProperty("os.name");
        if(osname.startsWith("Mac")){
            dllPath = "lib/macosx";
            checkJoalLib(dllPath);
        }else{ //windows
            String arc = System.getProperty("os.arch").endsWith("64") ? "x86_64" : "x86";
            dllPath = "lib/win32-" + arc;
            checkJnaDll(dllPath);
            checkJoalDll(dllPath);
        }
    }

    private void initSound() {
        try {
            SoundFactory.invokeAfterSound(soundFactory.getSound(resourceBundle.getString("sound.init")), new Runnable() {
                @Override
                public void run() {
                    //nothing to do, just play a sound
                }
            });
        } catch (final Exception e) {
            Main.log.log(Level.WARNING, resourceBundle.getString("exception.init_sound_failed"));
        }
    }

    /**
     * theme.getResource(resourceBundle.getString("image.mouse_icon"))
     *
     * @return
     */
    protected abstract URL getResResource(String name);
    public final Cursor prepareCursor(final URL url,final int hotSpotX,final int hotSpotY){
        BufferedImage cursorImage = null;
        try {
            cursorImage = ImageIO.read(url);
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
        String cursorName = "VocalShimejiCursor";
        return Toolkit.getDefaultToolkit().createCustomCursor(cursorImage, new Point(hotSpotX,hotSpotY), cursorName);
    }
    public void run() {
        applyUserConfig(userConfig);
        initSound();
        loadConfiguration();

        cursor = prepareCursor(
                getResResource(resourceBundle.getString("shimeji.mouse_cursor_resname")),
                Integer.parseInt(resourceBundle.getString("shimeji.mouse_cursor_hotspot_x")),
                Integer.parseInt(resourceBundle.getString("shimeji.mouse_cursor_hotspot_y"))
        );
        cursorPressed = prepareCursor(
                getResResource(resourceBundle.getString("shimeji.mouse_cursor_pressed_resname")),
                Integer.parseInt(resourceBundle.getString("shimeji.mouse_cursor_pressed_hotspot_x")),
                Integer.parseInt(resourceBundle.getString("shimeji.mouse_cursor_pressed_hotspot_y"))
        );
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
            log.log(Level.WARNING, resourceBundle.getString("exception.init_tray_icon_failed"), e);
        }
        manager.createMascot();
        manager.start();
    }

    public void scrnSaveRun() {
        applyUserConfig(ReadUserConfig(resourceBundle));
        initSound();
        loadConfiguration();
        manager.createMascot();
        manager.start();
    }

    public static void showConfigRun(final ResourceBundle resourceBundle) {
        final UserConfig userConfig = ReadUserConfig(resourceBundle);
        new ConfigDialog(userConfig,resourceBundle).setVisible(true);
        System.exit(0);
    }

    /**
     * Main.getInstance().theme.getResourceAsStream("conf/Behavior.xml")
     * Main.getInstance().theme.getResourceAsStream("conf/Actions.xml")
     * @param name
     * @return
     */
    protected abstract InputStream getConfigStream(String name);
    private void loadConfiguration() {
        try {
            log.log(Level.INFO, resourceBundle.getString("message.loading_config"), "Behavior.xml");

            Document actions = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(getConfigStream("Behavior.xml"));

            log.log(Level.INFO, resourceBundle.getString("message.loading_config"), "Actions.xml");

            getConfiguration().load(imagePairLoader, soundFactory, new Entry(actions.getDocumentElement()));

            Document behaviors = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(getConfigStream("Actions.xml"));

            getConfiguration().load(imagePairLoader, soundFactory, new Entry(behaviors.getDocumentElement()));

            getConfiguration().validate();
        } catch (IOException e) {
            log.log(Level.SEVERE, resourceBundle.getString("exception.load_config_failed"), e);
            exit();
        } catch (SAXException e) {
            log.log(Level.SEVERE, resourceBundle.getString("exception.load_config_failed"), e);
            exit();
        } catch (ParserConfigurationException e) {
            log.log(Level.SEVERE, resourceBundle.getString("exception.load_config_failed"), e);
            exit();
        } catch (ConfigurationException e) {
            log.log(Level.SEVERE, resourceBundle.getString("exception.load_config_failed"), e);
            exit();
        }
    }

    private void createTrayIcon() {
        log.log(Level.INFO, resourceBundle.getString("message.create_tray_icon"));
        if (SystemTray.getSystemTray() == null) {
            return;
        }
        mascotPopupMenu.prepareMainMenu(mainMenu);
        try {
            TrayIcon icon = new TrayIcon(ImageIO.read(getResResource(resourceBundle.getString("shimeji.tray_icon_resname"))), "VocalShimeji", mainMenu);
            mascotPopupMenu.prepareTrayIcon(icon);
            SystemTray.getSystemTray().add(icon);
        } catch (IOException e) {
            log.log(Level.SEVERE, resourceBundle.getString("exception.create_tray_icon_failed"), e);
            exit();
        } catch (AWTException e) {
            log.log(Level.SEVERE, resourceBundle.getString("exception.create_tray_icon_failed"), e);
            mascotPopupMenu.setShowSystemTrayMenu(true);
            manager.setExitOnLastRemoved(true);
        }
    }

    protected abstract void showAbout();

    public void gatherAll() {
        manager.setBehaviorAll(getConfiguration(), BEHAVIOR_GATHER);
    }

    public void remainOne() {
        manager.remainOne();
    }

    public void exit() {
        manager.disposeAll();
        System.exit(0);
    }

    public Configuration getConfiguration() {
        return this.configuration;
    }

    public abstract URL getSoundResource(String resName);

    public abstract URL getImageResource(String name);

    public void showConfigDlg() {
        new ConfigDialog(userConfig,resourceBundle).setVisible(true);
    }

    private void applyUserConfig(final UserConfig userConfig) {
        soundFactory.sfxOn = userConfig.sfxOn;
        mascotPopupMenu.sfxMenu.setState(userConfig.sfxOn);
        mascotPopupMenu.jSfxMenu.setState(userConfig.sfxOn);

        soundFactory.voiceOn = userConfig.voiceOn;
        mascotPopupMenu.voiceMenu.setState(userConfig.voiceOn);
        mascotPopupMenu.jVoiceMenu.setState(userConfig.voiceOn);

        if (soundFactory.sfxGain != userConfig.sfxGain)
            soundFactory.setSfxGain(userConfig.sfxGain);

        if (soundFactory.voiceGain != userConfig.voiceGain)
            soundFactory.setVoiceGain(userConfig.voiceGain);
    }

    public static class UTF8ResourceBundleControl extends ResourceBundle.Control {
        @Override
        public ResourceBundle newBundle(String baseName, Locale locale, String format, ClassLoader loader, boolean reload) throws IllegalAccessException, InstantiationException, IOException {
            // The below is a copy of the default implementation.
            String bundleName = toBundleName(baseName, locale);
            String resourceName = toResourceName(bundleName, "properties");
            ResourceBundle bundle = null;
            InputStream stream = null;
            if (reload) {
                URL url = loader.getResource(resourceName);
                if (url != null) {
                    URLConnection connection = url.openConnection();
                    if (connection != null) {
                        connection.setUseCaches(false);
                        stream = connection.getInputStream();
                    }
                }
            } else {
                stream = loader.getResourceAsStream(resourceName);
            }
            if (stream != null) {
                try {
                    // Only this line is changed to make it to read properties files as UTF-8.
                    bundle = new PropertyResourceBundle(new InputStreamReader(stream, "UTF-8"));
                } finally {
                    stream.close();
                }
            }
            return bundle;
        }
    }
}