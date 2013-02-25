package com.group_finity.mascot;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
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
    private JLabel voiceGainLabel;
    private JLabel sfxGainLabel;
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
        voiceSlider.setValue((int) (20 * Math.log10(config.voiceGain)));
        voiceGainLabel.setText(String.format("%+03ddb",voiceSlider.getValue()));
        voiceSlider.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                voiceGainLabel.setText(String.format("%+03ddb",voiceSlider.getValue()));
            }
        });
        sfxCheckBox.setSelected(config.sfxOn);
        sfxSlider.setValue((int) (int) (20 * Math.log10(config.sfxGain)));
        sfxGainLabel.setText(String.format("%+03ddb",sfxSlider.getValue()));
        sfxSlider.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {

                sfxGainLabel.setText(String.format("%+03ddb",sfxSlider.getValue()));
            }
        });
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
        config.voiceGain = (float) (Math.pow(10,voiceSlider.getValue()/20.0));
        config.sfxGain = (float) (Math.pow(10,sfxSlider.getValue()/20.0));
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
