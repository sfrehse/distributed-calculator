package com.hivemq.mqtt.dcalc.engine;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hivemq.client.mqtt.MqttGlobalPublishFilter;
import com.hivemq.client.mqtt.datatypes.MqttQos;
import com.hivemq.client.mqtt.mqtt5.Mqtt5BlockingClient;
import com.hivemq.client.mqtt.mqtt5.Mqtt5Client;
import com.hivemq.client.mqtt.mqtt5.message.publish.Mqtt5Publish;
import com.hivemq.mqtt.dcalc.engine.model.ExpressionResult;
import com.hivemq.mqtt.dcalc.runtime.model.Program;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class MqttSerializer extends Thread {
    private final String topic;

    private final String responseTopic = "calculate_response";
    private final Mqtt5BlockingClient client;

    private final Map<String, Program> correlationMap = new ConcurrentHashMap<>();
    private final ResultReceivedCallback resultReceivedCallback;

    private final ObjectMapper objectMapper = new ObjectMapper();

    public MqttSerializer(Mqtt5BlockingClient client, String topic, ResultReceivedCallback resultReceivedCallback) {
        this.client = Objects.requireNonNull(client);
        this.topic = Objects.requireNonNull(topic);
        this.resultReceivedCallback = Objects.requireNonNull(resultReceivedCallback);
    }

    public MqttSerializer(Mqtt5BlockingClient client, String topic) {
        this.client = Objects.requireNonNull(client);
        this.topic = Objects.requireNonNull(topic);
        this.resultReceivedCallback = null;
    }

    @Override
    public void run() {
        final Mqtt5BlockingClient client = Mqtt5Client.builder()
                .identifier(UUID.randomUUID().toString())
                .serverHost("Stefans-MBP.fritz.box")
                .buildBlocking();
        client.connect();

        try (Mqtt5BlockingClient.Mqtt5Publishes publishes = client.publishes(MqttGlobalPublishFilter.ALL)) {
            client.subscribeWith().topicFilter("calculate_response").qos(MqttQos.AT_LEAST_ONCE)
                    .send();
            System.out.println("Ready for responses");

            while (true) {
                Mqtt5Publish publishMessage = publishes.receive();
                final ExpressionResult expressionResult = objectMapper.readValue(new String(publishMessage.getPayloadAsBytes()), ExpressionResult.class);

                Optional<ByteBuffer> correlationIdBytes = publishMessage.getCorrelationData();

                correlationIdBytes.ifPresent(id -> {
                    String correlationId = new String(StandardCharsets.UTF_8.decode(publishMessage.getCorrelationData().get()).array());
                    if (correlationMap.containsKey(correlationId)) {
                        resultReceivedCallback.callback(correlationId, expressionResult);
                    } else {
                        System.out.printf("There is no correlationId %s found%n", correlationId);
                    }
                });
            }
        } catch (Exception exception) {
            System.out.printf("Error while executing program %s%n", exception.getMessage());
        }
    }

    public void serialize(final Program program, String correlationId) {
        assert !correlationId.isEmpty();
        try {
            final ObjectMapper objectMapper = new ObjectMapper();
            final String ret = objectMapper.writeValueAsString(program);

            correlationMap.put(correlationId, program);

            client.publishWith()
                    .topic(topic)
                    .correlationData(correlationId.getBytes())
                    .responseTopic(responseTopic)
                    .payload(ret.getBytes()).send();


        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
