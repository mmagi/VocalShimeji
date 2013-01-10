package com.group_finity.mascot.action;

import com.group_finity.mascot.Mascot;
import com.group_finity.mascot.animation.Animation;
import com.group_finity.mascot.exception.VariableException;
import com.group_finity.mascot.script.VariableMap;

import java.util.ArrayList;

/**
 * マスコットの状態を変更するだけで一瞬で終わるアクションの基底クラス.
 *
 * @author Yuki Yamada
 */
public abstract class InstantAction extends ActionBase {

    public InstantAction(final VariableMap params) {
        super(new ArrayList<Animation>(), params);

    }

    @Override
    public final void init(final Mascot mascot) throws VariableException {
        super.init(mascot);

        if (super.hasNext()) {
            apply();
        }
    }

    protected abstract void apply() throws VariableException;

    @Override
    public final boolean hasNext() throws VariableException {
        //super.hasNext();
        return false;
    }

    @Override
    protected final void tick() {
    }
}
