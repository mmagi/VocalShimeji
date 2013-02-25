package com.group_finity.mascot;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;


public class ConfigDialog extends JDialog {
    private JPanel contentPane;
    private JButton buttonOK;
    private JButton buttonCancel;
    private JCheckBox voiceCheckBox;
    private JSlider voiceSlider;
    private JCheckBox sfxCheckBox;
    private JSlider sfxSlider;
    private final Main main;

    public ConfigDialog(Main main) {
        this.main = main;
        setContentPane(contentPane);
        setModal(true);
        getRootPane().setDefaultButton(buttonOK);
        voiceCheckBox.setText(this.main.resourceBundle.getString("menu.checkbox_voice"));
        voiceCheckBox.setSelected(this.main.soundFactory.voiceOn);
        voiceSlider.setValue((int) (this.main.soundFactory.voiceGain * 100));
        sfxCheckBox.setText(this.main.resourceBundle.getString("menu.checkbox_sfx"));
        sfxCheckBox.setSelected(this.main.soundFactory.sfxOn);
        sfxSlider.setValue((int) (this.main.soundFactory.sfxGain * 100));
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
    }

    private void onOK() {
// add your code here
        dispose();
    }

    private void onCancel() {
// add your code here if necessary
        dispose();
    }

}
