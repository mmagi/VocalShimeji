import com.group_finity.mascot.Main;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

/**
 * Created with IntelliJ IDEA.
 * User: magi
 * Date: 12-12-27
 * Time: 下午3:42
 */
public class Hijikata_Toushirou {

    static final Logger log = Logger.getLogger(Hijikata_Toushirou.class.getName());
    private static final SplashScreen splashScreen = new SplashScreen();

    public static void main(String[] args) {
        LogHandler.setJTextArea(splashScreen.textArea);
        splashScreen.setVisible(true);
        try {
            LogManager.getLogManager().readConfiguration(Main.class.getResourceAsStream("/conf/logging.properties"));
        } catch (SecurityException ignored) {
        } catch (IOException ignored) {
        }
        try {
            checkJnaDll();
            Main.getInstance().setAboutScreen(new AboutScreen());
            Main.getInstance().run();
        } catch (final Throwable e) {
            log.log(Level.ALL, "启动时出现错误", e);
            splashScreen.reportErrorAndExit(e);
        }
        splashScreen.dispose();
    }

    private static void checkJnaDll() {
        String arc = System.getProperty("os.arch").endsWith("64") ? "x86_64" : "x86";
        String dir = "lib/win32-" + arc;
        File jnaLib = new File(new File(dir), System.mapLibraryName("jnidispatch"));
        if (jnaLib.exists())
            System.setProperty("jna.boot.library.path", dir);
        else {
            throw new RuntimeException(jnaLib.getAbsolutePath() + "文件未找到.");
        }
    }

}
