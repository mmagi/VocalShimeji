package vocalshimeji.theme.kagura;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.io.PrintWriter;
import java.io.StringWriter;

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
        ImageIcon logo = new ImageIcon(SplashScreen.class.getResource("res/splash.png"));
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

    public void reportErrorAndExit(final Throwable e) {
        this.setAlwaysOnTop(false);
        if (JOptionPane.OK_OPTION == JOptionPane.showConfirmDialog(this, "糟糕了，启动的时候出错啦。\n点击确定把错误信息复制到剪贴版。\n" + "请把错误信息粘贴给支持人员们以便更快的定位问题。", "错误", JOptionPane.OK_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE)) {
            final StringWriter sw = new StringWriter();
            final PrintWriter pw = new PrintWriter(sw);
            e.printStackTrace(pw);
            pw.close();
            StringSelection selection = new StringSelection(sw.toString());
            Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
            clipboard.setContents(selection, selection);
        }
        System.exit(-1);
    }

}
