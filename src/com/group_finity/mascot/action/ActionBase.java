/**/
package com.group_finity.mascot.action;

import com.group_finity.mascot.Mascot;
import com.group_finity.mascot.SoundManager;
import com.group_finity.mascot.animation.Animation;
import com.group_finity.mascot.environment.MascotEnvironment;
import com.group_finity.mascot.exception.LostGroundException;
import com.group_finity.mascot.exception.VariableException;
import com.group_finity.mascot.script.Variable;
import com.group_finity.mascot.script.VariableMap;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public abstract class ActionBase implements Action {
    private static final Logger log = Logger.getLogger(ActionBase.class.getName());
    public static final String PARAMETER_DURATION = "長さ";
    private static final boolean DEFAULT_CONDITION = true;
    public static final String PARAMETER_CONDITION = "条件";
    private static final int DEFAULT_DURATION = 2147483647;
    private Mascot mascot;
    private int startTime;
    private List<Animation> animations;
    private VariableMap variables;

    public ActionBase(List<Animation> animations, VariableMap context) {
        this.animations = animations;
        this.variables = context;
    }

    public String toString() {
        try {
            return "動作(" + getClass().getSimpleName() + "," + getName() + ")";
        } catch (VariableException e) {
        }
        return "動作(" + getClass().getSimpleName() + "," + null + ")";
    }

    public void init(Mascot mascot) throws VariableException {
        setMascot(mascot);
        setTime(0);
        log.log(Level.INFO, "動作開始({0},{1})", new Object[]{getMascot(), this});
        getVariables().put("mascot", mascot);
        getVariables().put("action", this);
        if (SoundManager.voiceOn) {
            Object sound = getVariables().get("voice");
            if (null != sound && sound.toString().length() > 0) {
                String soundFile = sound.toString();
                int pri = -10;
                Object priority = getVariables().get("priority");
                if ((priority instanceof Double)) {
                    pri = ((Double) priority).intValue();
                }
                mascot.voiceDemon.speak(soundFile, pri);
            }
        }
        getVariables().init();

        for (Animation animation : this.animations)
            animation.init();
    }

    public void next() throws LostGroundException, VariableException {
        initFrame();
        tick();
    }

    private void initFrame() {
        getVariables().initFrame();

        for (Animation animation : getAnimations())
            animation.initFrame();
    }

    private List<Animation> getAnimations() {
        return this.animations;
    }

    protected abstract void tick() throws LostGroundException, VariableException;

    public boolean hasNext() throws VariableException {
        boolean effective = isEffective().booleanValue();
        boolean intime = getTime() < getDuration();

        return (effective) && (intime);
    }

    private Boolean isEffective() throws VariableException {
        return (Boolean) eval("条件", Boolean.class, Boolean.valueOf(true));
    }

    private int getDuration() throws VariableException {
        return ((Number) eval("長さ", Number.class, Integer.valueOf(2147483647))).intValue();
    }

    private void setMascot(Mascot mascot) {
        this.mascot = mascot;
    }

    protected Mascot getMascot() {
        return this.mascot;
    }

    protected int getTime() {
        return getMascot().getTime() - this.startTime;
    }

    protected void setTime(int time) {
        this.startTime = (getMascot().getTime() - time);
    }

    private String getName() throws VariableException {
        return (String) eval("名前", String.class, null);
    }

    protected Animation getAnimation() throws VariableException {
        for (Animation animation : getAnimations()) {
            if (animation.isEffective(getVariables())) {
                if (SoundManager.voiceOn && null != animation.getVoice() && mascot.voiceDemon.getLastPlayed() != animation.getVoice()) {// 不重复播放同一个
                    mascot.voiceDemon.speak(animation.getVoice(), animation.getVoicePriority());
                }
                return animation;
            }
        }

        log.log(Level.SEVERE, "有効なアニメーションが見つけられませんでした({0},{1})", new Object[]{getMascot(), this});
        return null;
    }

    private VariableMap getVariables() {
        return this.variables;
    }

    protected void putVariable(String key, Object value) {
        synchronized (getVariables()) {
            getVariables().put(key, value);
        }
    }

    protected <T> T eval(String name, Class<T> type, T defaultValue) throws VariableException {
        synchronized (getVariables()) {
            Variable variable = (Variable) getVariables().getRawMap().get(name);
            if (variable != null) {
                return type.cast(variable.get(getVariables()));
            }
        }

        return defaultValue;
    }

    protected MascotEnvironment getEnvironment() {
        return getMascot().getEnvironment();
    }
}