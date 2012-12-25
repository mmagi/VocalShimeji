package com.group_finity.mascot.config;

import com.group_finity.mascot.action.Action;
import com.group_finity.mascot.exception.ActionInstantiationException;
import com.group_finity.mascot.exception.ConfigurationException;

import java.util.Map;

public interface IActionBuilder {

    public void validate() throws ConfigurationException;

    public Action buildAction(final Map<String, String> params) throws ActionInstantiationException;

}
