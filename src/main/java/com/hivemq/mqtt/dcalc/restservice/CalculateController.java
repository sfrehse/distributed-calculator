package com.hivemq.mqtt.dcalc.restservice;

import com.hivemq.mqtt.dcalc.engine.model.ExpressionResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
public class CalculateController implements ResultCallback {
    private final Map<String, ExpressionResult> expressionResultMap = new HashMap<>();

    @Autowired
    private CalculateService calculateService;

    @PostMapping(value = "/submit", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public CalculateResponse submit(@RequestBody CalculateRequest calculateRequest) {
        final String expressionStr = calculateRequest.getExpression();
        final String correlationId = UUID.randomUUID().toString();

        calculateService.submitData(expressionStr, correlationId, this);

        try {
            while (true) {
                if (expressionResultMap.containsKey(correlationId)) {
                    return new CalculateResponse(expressionResultMap.get(correlationId).result());
                }

                Thread.sleep(Duration.ofMillis(100).toMillis());
            }
        } catch (Exception exception) {
            throw new RuntimeException("Error while waiting for result: %s".formatted(exception));
        }
    }

    @Override
    public void callback(String correlationId, ExpressionResult result) {
        expressionResultMap.put(correlationId, result);
    }
}
