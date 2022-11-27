package com.hivemq.mqtt.dcalc.expression;

import com.hivemq.mqtt.dcalc.ExpressionParser;

import java.util.Objects;

public class ArithmeticBinaryExpression extends Expression {

    private final Expression lhs;
    private final OperatorType operator;
    private final Expression rhs;

    public ArithmeticBinaryExpression(
            final Expression lhs,
            final OperatorType operator,
            final Expression rhs,
            final String text
    ) {
        super(Objects.requireNonNull(text));
        this.lhs = Objects.requireNonNull(lhs);
        this.operator = Objects.requireNonNull(operator);
        this.rhs = Objects.requireNonNull(rhs);
    }

    public Expression getLhs() {
        return lhs;
    }

    public OperatorType getOperator() {
        return operator;
    }

    public Expression getRhs() {
        return rhs;
    }
}
