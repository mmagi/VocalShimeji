package com.group_finity.mascot.image;

import javax.swing.*;

public interface TranslucentWindow {

    public JWindow asJWindow();

    public void setImage(NativeImage image);

    public void updateImage();
}
