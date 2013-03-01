package vocalshimeji.theme.sakata_gintoki;

import javax.swing.*;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.logging.LogRecord;

/**
 * log写到欢迎窗口去，方便查错.
 * User: magi
 * Date: 12-12-25
 * Time: 下午3:26
 */
public class LogHandler extends java.util.logging.Handler {
    private static JTextArea output;

    public static void setJTextArea(JTextArea output) {
        LogHandler.output = output;
    }

    @Override
    public void publish(final LogRecord record) {
        if (null != output) {
            SwingUtilities.invokeLater(new Runnable() {

                @Override
                public void run() {
                    output.append(format(record));
                }

            });
        }
    }

    private String format(final LogRecord record) {
        // Minimize memory allocations here.
        final StringBuilder sb = new StringBuilder();
        sb.append(record.getLevel().getLocalizedName());
        sb.append(":");
        //        if (record.getSourceClassName() != null) {
        //            sb.append(record.getSourceClassName());
        //        } else {
        //            sb.append(record.getLoggerName());
        //        }
        //        if (record.getSourceMethodName() != null) {
        //            sb.append(" ");
        //            sb.append(record.getSourceMethodName());
        //        }
        //        sb.append(" ");

        sb.append(formatMessage(record));
        sb.append("\n");
        //noinspection ThrowableResultOfMethodCallIgnored
        if (record.getThrown() != null) {
            try {
                final StringWriter sw = new StringWriter();
                final PrintWriter pw = new PrintWriter(sw);
                //noinspection ThrowableResultOfMethodCallIgnored
                record.getThrown().printStackTrace(pw);
                pw.close();
                sb.append(sw.toString());
            } catch (final Exception ignored) {
            }
        }
        return sb.toString();
    }

    //复制LogFormatter中的标准实现
    public synchronized String formatMessage(LogRecord record) {
        String format = record.getMessage();
        java.util.ResourceBundle catalog = record.getResourceBundle();
        if (catalog != null) {
            //	    // We cache catalog lookups.  This is mostly to avoid the
            //	    // cost of exceptions for keys that are not in the catalog.
            //	    if (catalogCache == null) {
            //		catalogCache = new HashMap();
            //	    }
            //	    format = (String)catalogCache.get(record.essage);
            //	    if (format == null) {
            try {
                format = catalog.getString(record.getMessage());
            } catch (java.util.MissingResourceException ex) {
                // Drop through.  Use record message as format
                format = record.getMessage();
            }
            //		catalogCache.put(record.message, format);
            //	    }
        }
        // Do the formatting.
        try {
            Object parameters[] = record.getParameters();
            if (parameters == null || parameters.length == 0) {
                // No parameters.  Just return format string.
                return format;
            }
            // Is is a java.text style format?
            // Ideally we could match with
            // Pattern.compile("\\{\\d").matcher(format).find())
            // However the cost is 14% higher, so we cheaply check for
            // 1 of the first 4 parameters
            if (format.contains("{0") || format.contains("{1") ||
                    format.contains("{2") || format.contains("{3")) {
                return java.text.MessageFormat.format(format, parameters);
            }
            return format;

        } catch (Exception ex) {
            // Formatting failed: use localized format string.
            return format;
        }
    }

    @Override
    public void flush() {
    }

    @Override
    public void close() throws SecurityException {
    }

}
