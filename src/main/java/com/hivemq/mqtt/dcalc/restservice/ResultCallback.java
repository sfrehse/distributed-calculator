package com.hivemq.mqtt.dcalc.restservice;

import com.hivemq.mqtt.dcalc.engine.model.ExpressionResult;

interface ResultCallback {
    public void callback(String correlationId, ExpressionResult result);
}
