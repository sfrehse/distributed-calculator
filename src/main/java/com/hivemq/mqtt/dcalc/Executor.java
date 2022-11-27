package com.hivemq.mqtt.dcalc;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hivemq.client.mqtt.MqttGlobalPublishFilter;
import com.hivemq.client.mqtt.datatypes.MqttQos;
import com.hivemq.client.mqtt.datatypes.MqttTopic;
import com.hivemq.client.mqtt.mqtt5.Mqtt5BlockingClient;
import com.hivemq.client.mqtt.mqtt5.Mqtt5Client;
import com.hivemq.client.mqtt.mqtt5.message.publish.Mqtt5Publish;
import com.hivemq.mqtt.dcalc.engine.Evaluator;
import com.hivemq.mqtt.dcalc.engine.MqttSerializer;
import com.hivemq.mqtt.dcalc.engine.model.ExpressionResult;
import com.hivemq.mqtt.dcalc.runtime.model.Program;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.UUID;

public class Executor {
    final ObjectMapper objectMapper = new ObjectMapper();

    public static void main(String[] args) {
        final Evaluator evaluator = new Evaluator();
        final Executor executor = new Executor();
        executor.run(evaluator);
    }

    public void run(Evaluator evaluator) {

        final Mqtt5BlockingClient client = Mqtt5Client.builder()
                .identifier(UUID.randomUUID().toString())
                .serverHost("Stefans-MBP.fritz.box")
                .buildBlocking();
        client.connect();
        System.out.println("Connected");

        try (Mqtt5BlockingClient.Mqtt5Publishes publishes = client.publishes(MqttGlobalPublishFilter.ALL)) {
            client.subscribeWith().topicFilter("$share/gr1/calculate").qos(MqttQos.AT_LEAST_ONCE)
                    .send();
            System.out.println("Subscribed");

            while (true) {
                Mqtt5Publish publishMessage = publishes.receive();
                assert publishMessage.getCorrelationData().isPresent();

                String correlationId = new String(StandardCharsets.UTF_8.decode(publishMessage.getCorrelationData().get()).array());
                System.out.print(".");

                final byte[] payload = publishMessage.getPayloadAsBytes();
                final Program program = handlePayload(evaluator, payload, client, correlationId, publishMessage.getResponseTopic());
                if (program != null) {
                    final MqttSerializer serializer = new MqttSerializer(client, "calculate");
                    serializer.serialize(program, correlationId);
                } else {
                    System.out.println("T");
                }
            }
        } catch (InterruptedException exception) {
            System.out.printf("An error occurred while subscribing %s%n", exception.getMessage());
        }

    }

    private Program handlePayload(Evaluator evaluator, byte[] payload, Mqtt5BlockingClient client, String correlationId, Optional<MqttTopic> responseTopic) {
        try {
            final Program program = objectMapper.readValue(new String(payload), Program.class);

            final Program appliedProgram = evaluator.eval(program);
            if (appliedProgram.pc() == appliedProgram.instructions().size()) {
                final double finalValue = appliedProgram.instructions().get(appliedProgram.pc() - 1).result();

                responseTopic.ifPresent(topic -> {
                    final ExpressionResult expressionResult = new ExpressionResult(String.valueOf(finalValue));
                    final String response;

                    try {
                        response = objectMapper.writeValueAsString(expressionResult);
                    } catch (JsonProcessingException e) {
                        throw new RuntimeException(e);
                    }

                    client.publishWith()
                            .topic(topic)
                            .correlationData(correlationId.getBytes())
                            .payload(response.getBytes()).send();
                });

                return null;
            } else {
                return appliedProgram;
            }
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}