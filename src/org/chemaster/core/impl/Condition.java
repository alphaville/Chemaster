package org.chemaster.core.impl;

import org.chemaster.core.ICondition;

/**
 *
 * @author chung
 */
public class Condition implements ICondition {

    private String name;
    private String strVal;
    private Double dbl_val = null;
    private ConditionType type;

    @Override
    public ICondition setName(String name) {
        this.name = name;
        return this;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public ICondition setType(ConditionType type) {
        this.type = type;
        return this;
    }

    @Override
    public ConditionType getConditionType() {
        return type;
    }

    @Override
    public String getValueAsString() {
        if (strVal != null) {
            return strVal;
        }
        if (dbl_val != null) {
            return dbl_val.toString();
        }
        return null;
    }

    @Override
    public ICondition setValue(String string) {
        this.strVal = string;
        return this;
    }

    @Override
    public ICondition setValue(double dbl) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
