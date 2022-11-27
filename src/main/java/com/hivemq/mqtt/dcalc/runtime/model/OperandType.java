package com.hivemq.mqtt.dcalc.runtime.model;

public enum OperandType {
    Reference("$ref"), Constant("constant");

    private final String type;

    OperandType(String type) {
        this.type = type;
    }
}
