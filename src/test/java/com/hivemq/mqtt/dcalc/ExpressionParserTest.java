package com.hivemq.mqtt.dcalc;

import com.hivemq.mqtt.dcalc.expression.*;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TestScenario {
    final String expr;
    final OperatorType operatorType;

    public TestScenario(String expr, OperatorType operatorType) {
        this.expr = expr;
        this.operatorType = operatorType;
    }
}

class ExpressionParserTest {
    private final ExpressionParser expressionParser = new ExpressionParser();

    @Test
    void testSimpleExpressions() {
        final Expression constant = expressionParser.parse("10");
        assertTrue(constant instanceof DoubleConstantNodeExpression);


        Arrays.asList(
                new TestScenario("1 + 2", OperatorType.PLUS),
                new TestScenario("1-2", OperatorType.MINUS),
                new TestScenario("1*2", OperatorType.MULTIPLY),
                new TestScenario("1/2", OperatorType.DIVIDE)).forEach(expr -> {
            final Expression binaryPlus = expressionParser.parse(expr.expr);
            assertTrue(binaryPlus instanceof ArithmeticBinaryExpression);
            final ArithmeticBinaryExpression arithmeticBinaryPlusExpression = (ArithmeticBinaryExpression) binaryPlus;
            assertTrue(arithmeticBinaryPlusExpression.getLhs() instanceof DoubleConstantNodeExpression);
            assertTrue(arithmeticBinaryPlusExpression.getRhs() instanceof DoubleConstantNodeExpression);
            assertEquals(arithmeticBinaryPlusExpression.getOperator(), expr.operatorType);
        });
    }

    @Test
    void testSimpleFunctionCall() {
        final Expression expression = expressionParser.parse("SIN(10)");
        assertTrue(expression instanceof FunctionCall);
        final FunctionCall functionCall = (FunctionCall) expression;
        assertEquals(1, functionCall.getArguments().size());

        final Expression firstArgument = functionCall.getArguments().get(0);
        assertTrue(firstArgument instanceof DoubleConstantNodeExpression);
    }

    @Test
    void testSimpleFunctionCallWithTwoArguments() {
        final Expression expression = expressionParser.parse("POW(10,1)");
        assertTrue(expression instanceof FunctionCall);
        final FunctionCall functionCall = (FunctionCall) expression;
        assertEquals(2, functionCall.getArguments().size());

        final Expression firstArgument = functionCall.getArguments().get(0);
        assertTrue(firstArgument instanceof DoubleConstantNodeExpression);

        final Expression secondArgument = functionCall.getArguments().get(0);
        assertTrue(secondArgument instanceof DoubleConstantNodeExpression);
    }
}