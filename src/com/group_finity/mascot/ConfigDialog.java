package com.group_finity.mascot;

import javax.swing.*;
import java.awt.event.*;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;


public class ConfigDialog extends JDialog {
    static final Logger log = Logger.getLogger(JDialog.class.getName());
    private JPanel contentPane;
    private JButton buttonOK;
    private JButton buttonCancel;
    private JCheckBox voiceCheckBox;
    private JSlider voiceSlider;
    private JCheckBox sfxCheckBox;
    private JSlider sfxSlider;
    private final UserConfig config;

    public ConfigDialog(UserConfig conf, ResourceBundle resourceBundle) {
        this.config = conf;
        setContentPane(contentPane);
        setModal(true);
        getRootPane().setDefaultButton(buttonOK);
        try {
            setTitle(resourceBundle.getString("configDlg.title"));
            voiceCheckBox.setText(resourceBundle.getString("configDlg.voice"));
            sfxCheckBox.setText(resourceBundle.getString("configDlg.sfx"));
        } catch (Exception ignored) {
        }
        voiceCheckBox.setSelected(config.voiceOn);
        voiceSlider.setValue((int) (config.voiceGain * 100));
        sfxCheckBox.setSelected(config.sfxOn);
        sfxSlider.setValue((int) (config.sfxGain * 100));
        buttonOK.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onOK();
            }
        });
        buttonCancel.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onCancel();
            }
        });

// call onCancel() when cross is clicked
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                onCancel();
            }
        });

// call onCancel() on ESCAPE
        contentPane.registerKeyboardAction(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onCancel();
            }
        }, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        setLocationRelativeTo(null);
        pack();
    }

    private void onOK() {
        dispose();
        config.voiceOn = voiceCheckBox.isSelected();
        config.sfxOn = sfxCheckBox.isSelected();
        config.voiceGain = voiceSlider.getValue() / 100f;
        config.sfxGain = sfxSlider.getValue() / 100f;
        for (UserConfig.Callback callback : config.onConfigUpdated)
            try {
                callback.onConfigUpdated(config);
            } catch (Exception e) {
                log.log(Level.WARNING, "Exec Config CallBack Failed", e);
            }
    }

    private void onCancel() {
        dispose();
    }

}
