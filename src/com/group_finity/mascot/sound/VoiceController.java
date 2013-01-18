package com.group_finity.mascot.sound;

/**
 * Created with IntelliJ IDEA.
 * User: magi
 * Date: 12-12-27
 * Time: 上午9:29
 */
public interface VoiceController {
    public void speak(final SoundBuffer voice, final int pri);

    public void release();

    public SoundBuffer getLastPlayed();
}
