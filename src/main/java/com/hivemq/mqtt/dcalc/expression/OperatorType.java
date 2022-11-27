package com.hivemq.mqtt.dcalc.expression;

public enum OperatorType {
    PLUS("+"), MINUS("-"),
    MULTIPLY("*"), DIVIDE("/"),
    IDENTITY("ID"),

    SIN("SIN");

    private final String op;

    OperatorType(String op) {
        this.op = op;
    }

    public String getOp() {
        return op;
    }
}
