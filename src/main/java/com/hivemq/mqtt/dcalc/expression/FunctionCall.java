package com.hivemq.mqtt.dcalc.expression;

import java.util.List;
import java.util.Objects;
import java.util.function.Function;

public class FunctionCall extends Expression {

    private final String functionName;
    private final List<Expression> arguments;

    public FunctionCall(String functionName, List<Expression> arguments, String text) {
        super(text);

        this.functionName = Objects.requireNonNull(functionName);
        this.arguments = Objects.requireNonNull(arguments);
    }

    public String getFunctionName() {
        return functionName;
    }

    public List<Expression> getArguments() {
        return arguments;
    }
}
