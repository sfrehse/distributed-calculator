package com.hivemq.mqtt.dcalc.expression;

import java.util.Objects;

public class DoubleConstantNodeExpression extends ConstantNodeExpression {
    private final Double value;

    public DoubleConstantNodeExpression(Double value, String text) {
        super(Objects.requireNonNull(text));
        this.value = Objects.requireNonNull(value);
    }

    public Double getValue() {
        return value;
    }
}
