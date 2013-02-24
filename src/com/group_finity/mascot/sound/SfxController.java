package com.group_finity.mascot.sound;

import com.group_finity.mascot.environment.Area;

import java.awt.*;

/**
 * Created with IntelliJ IDEA.
 * User: magi
 * Date: 12-12-27
 * Time: 上午9:25
 */
public interface SfxController {
    public void sound(final SoundBuffer sound);

    public void updatePosition(final Point anchor, final Area area);

    public void release();

    public void onGainChanged();
}
