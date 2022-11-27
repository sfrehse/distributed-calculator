package com.hivemq.mqtt.dcalc.runtime.model;

import com.hivemq.mqtt.dcalc.expression.Expression;

public record Operand(OperandType type, String value) {
}
