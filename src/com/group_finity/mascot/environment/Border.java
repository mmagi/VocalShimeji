package com.group_finity.mascot.environment;

import java.awt.*;

public interface Border {

    public boolean isOn(Point location);

    public Point move(Point location);
}
