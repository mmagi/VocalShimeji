package com.group_finity.mascot;

import javax.xml.bind.annotation.*;
import java.util.ArrayList;

/**
 * Created with IntelliJ IDEA.
 * User: magi
 * Date: 13-2-25
 * Time: 上午9:17
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType
public class UserConfig {
    public boolean sfxOn = true;
    public float sfxGain = 1.0f;
    public boolean voiceOn = true;
    public float voiceGain = 1.0f;
    public boolean forbidPushIE = false;
    public boolean forbidFork = false;
    @XmlTransient
    public final ArrayList<Callback> onConfigUpdated = new ArrayList<Callback>();
    public static interface Callback{
        public abstract void onConfigUpdated (final UserConfig config) throws Exception ;
    }
}
