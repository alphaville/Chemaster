package org.chemaster.core;

/**
 *
 * @author chung
 */
public interface ICondition {

    enum ConditionType {

        STRING,
        DOUBLE,
        BINARY;
    }

    ICondition setName(String name);

    String getName();

    ICondition setType(ConditionType type);

    ConditionType getConditionType();

    String getValueAsString();

    ICondition setValue(String string);

    ICondition setValue(double dbl);
}
