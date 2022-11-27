package com.hivemq.mqtt.dcalc.engine;

import com.hivemq.mqtt.dcalc.engine.model.ExpressionResult;

public interface ResultReceivedCallback {
    public void callback(String correlationId, ExpressionResult result);
}
