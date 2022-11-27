package com.hivemq.mqtt.dcalc.runtime.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.hivemq.mqtt.dcalc.expression.OperatorType;

import java.util.List;

public record OperationInstance(
        @JsonProperty("operatorType") OperatorType operatorType,
        @JsonProperty("operands") List<Operand> operands,
        @JsonProperty("result") Double result) {
}
