package com.group_finity.mascot.action;

import com.group_finity.mascot.script.VariableMap;

import java.util.logging.Logger;

/**
 * 複数のアクションから条件に一致する一つだけを実行するアクション.
 *
 * @author Yuki Yamada
 */
public class Select extends ComplexAction {

    private static final Logger log = Logger.getLogger(Select.class.getName());

    public Select(final VariableMap params, final Action... actions) {
        super(params, actions);
    }

}
