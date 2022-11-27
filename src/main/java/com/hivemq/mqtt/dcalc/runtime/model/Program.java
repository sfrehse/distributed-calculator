package com.hivemq.mqtt.dcalc.runtime.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record Program(
        int pc,
        @JsonProperty("inst")
        List<OperationInstance> instructions) {
}
