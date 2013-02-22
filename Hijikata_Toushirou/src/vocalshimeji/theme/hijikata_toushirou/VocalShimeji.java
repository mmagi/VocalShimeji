package vocalshimeji.theme.hijikata_toushirou;

import com.group_finity.mascot.Main;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

/**
 * Created with IntelliJ IDEA.
 * User: magi
 * Date: 12-12-27
 * Time: 下午3:42
 */
public class VocalShimeji extends Main {

    static final Logger log = Logger.getLogger(VocalShimeji.class.getName());
    private static final SplashScreen splashScreen = new SplashScreen();

    protected VocalShimeji() {
        super(ResourceBundle.getBundle(VocalShimeji.class.getName(), new UTF8ResourceBundleControl()));
    }

    public static void main(String[] args) {
        LoadDlls();
        int mode = 0;
        if (args.length > 0) {
            if (args[0].equals("--ScrnSave")) mode = 1;
            if (args[0].equals("--ShowConfig")) mode = -1;
        }
        if (mode >= 0) {
            LogHandler.setJTextArea(splashScreen.textArea);
            splashScreen.setVisible(true);
            try {
                LogManager.getLogManager().readConfiguration(VocalShimeji.class.getResourceAsStream("conf/logging.properties"));
            } catch (SecurityException ignored) {
            } catch (IOException ignored) {
            }
            try {
                if (mode == 1) {
                    new VocalShimeji().scrnSaveRun();
                } else {
                    new VocalShimeji().run();
                }
            } catch (final Throwable e) {
                log.log(Level.ALL, "启动时出现错误", e);
                splashScreen.reportErrorAndExit(e);
            }
            splashScreen.dispose();
        } else {
            showConfigRun();
        }
    }


    @Override
    protected InputStream getConfigStream(String name) {
        return VocalShimeji.class.getResourceAsStream("conf/" + name);
    }

    @Override
    protected URL getMouseIcon() {
        return VocalShimeji.class.getResource("res/mouse.png");
    }

    @Override
    protected URL getTrayIcon() {
        return VocalShimeji.class.getResource("res/icon.png");
    }

    final AboutScreen aboutScreen = new AboutScreen();

    @Override
    protected void showAbout() {
        aboutScreen.setVisible(true);
    }

    @Override
    public URL getSoundResource(String name) {
        return VocalShimeji.class.getResource("media/" + name);
    }

    @Override
    public URL getImageResource(String name) {
        return VocalShimeji.class.getResource("image/" + name);
    }
}
