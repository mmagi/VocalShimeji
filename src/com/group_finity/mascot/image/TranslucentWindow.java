package com.group_finity.mascot.image;

import javax.swing.*;

public interface TranslucentWindow {

    public JWindow asJWindow();

    public void setImage(NativeImage image);

    public void setPosition(int x, int y);

    public void updateWindow();

    public void dispose();

    public void setVisible(boolean b);

    public boolean isVisible();
}
