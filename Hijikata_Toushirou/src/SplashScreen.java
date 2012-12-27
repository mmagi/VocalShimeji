import javax.swing.*;
import java.awt.*;

/**
 * Created with IntelliJ IDEA.
 * User: magi
 * Date: 12-12-25
 * Time: 下午2:25
 */
public class SplashScreen extends JWindow {
    final JLabel imageLabel = new JLabel();
    final JTextArea textArea = new JTextArea(5, 20);
    static boolean closed = false;

    public SplashScreen() {
        ImageIcon logo = new ImageIcon(SplashScreen.class.getResource("/media/splash.png"));
        imageLabel.setIcon(logo);
        setLayout(new BorderLayout());
        add(imageLabel, BorderLayout.CENTER);
        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        add(scrollPane, BorderLayout.SOUTH);
        textArea.setAutoscrolls(true);
        pack();
        setLocationRelativeTo(null);
        setAlwaysOnTop(true);
    }

    @Override
    public void dispose() {
        closed = true;
        super.dispose();
    }

}
