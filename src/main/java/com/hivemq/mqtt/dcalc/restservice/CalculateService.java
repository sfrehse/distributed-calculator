package com.hivemq.mqtt.dcalc.restservice;

import com.hivemq.client.mqtt.mqtt5.Mqtt5BlockingClient;
import com.hivemq.client.mqtt.mqtt5.Mqtt5Client;
import com.hivemq.mqtt.dcalc.ExpressionParser;
import com.hivemq.mqtt.dcalc.engine.ExpressionBuilder;
import com.hivemq.mqtt.dcalc.engine.MqttSerializer;
import com.hivemq.mqtt.dcalc.engine.ResultReceivedCallback;
import com.hivemq.mqtt.dcalc.engine.model.ExpressionResult;
import com.hivemq.mqtt.dcalc.expression.Expression;
import com.hivemq.mqtt.dcalc.expression.examples.ExampleTaylorSeries;
import com.hivemq.mqtt.dcalc.runtime.model.OperationInstance;
import com.hivemq.mqtt.dcalc.runtime.model.Program;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Service
public class CalculateService implements ResultReceivedCallback {
    private Mqtt5BlockingClient client;
    private final Map<String, ResultCallback> correlationIdCallbacks = new ConcurrentHashMap<>();
    private MqttSerializer serializer;

    @Override
    public void callback(String correlationId, ExpressionResult result) {
        if (correlationIdCallbacks.containsKey(correlationId)) {
            correlationIdCallbacks.get(correlationId).callback(correlationId, result);
        } else {
            System.out.printf("Could not find correlationId %s%n", correlationId);
        }
    }

    public void submitData(String expressionString, String correlationId, ResultCallback callback) {
        assert !expressionString.isEmpty();
        assert !correlationId.isEmpty();

        final ExpressionParser parser = new ExpressionParser();
        final Expression expression = parser.parse(expressionString);

        final ExpressionBuilder expressionBuilder = new ExpressionBuilder();

        final List<OperationInstance> operationInstancesList = expressionBuilder.createStack(expression);
        final Program program = new Program(0, operationInstancesList);

        correlationIdCallbacks.put(correlationId, callback);

        serializer.serialize(program, correlationId);

    }

    @PostConstruct
    public void init() {
        System.out.println("Initialize Service");
        client = Mqtt5Client.builder()
                .identifier(UUID.randomUUID().toString())
                .serverHost("Stefans-MBP.fritz.box")
                .buildBlocking();
        client.connect();

        serializer = new MqttSerializer(client, "calculate", this);
        serializer.start();
    }
}
