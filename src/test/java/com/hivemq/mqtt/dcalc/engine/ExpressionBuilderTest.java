package com.hivemq.mqtt.dcalc.engine;

import com.hivemq.mqtt.dcalc.ExpressionParser;
import com.hivemq.mqtt.dcalc.expression.Expression;
import com.hivemq.mqtt.dcalc.expression.examples.FibonacciExample;
import com.hivemq.mqtt.dcalc.expression.OperatorType;
import com.hivemq.mqtt.dcalc.runtime.model.Operand;
import com.hivemq.mqtt.dcalc.runtime.model.OperandType;
import com.hivemq.mqtt.dcalc.runtime.model.OperationInstance;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;


class ExpressionBuilderTest {
    private final ExpressionParser expressionParser = new ExpressionParser();
    private final ExpressionBuilder expressionBuilder = new ExpressionBuilder();

    private String fibonacciSeries(int n) {
        return FibonacciExample.fibonacciSeries(n);
    }

    @Test
    void testSimpleExpressions() {
        final Expression expr = expressionParser.parse("10 * 3");

        final List<OperationInstance> instances = expressionBuilder.createStack(expr);
        assertEquals(3, instances.size());

        assertEquals(instances.get(0).operatorType(), OperatorType.IDENTITY);
        assertEquals(instances.get(0).result(), 10);
        assertEquals(0, instances.get(0).operands().size());

        assertEquals(instances.get(1).operatorType(), OperatorType.IDENTITY);
        assertEquals(instances.get(1).result(), 3);
        assertEquals(0, instances.get(1).operands().size());

        assertEquals(instances.get(2).operatorType(), OperatorType.MULTIPLY);
        final List<Operand> operands = instances.get(2).operands();
        assertEquals(2, operands.size());

        final Operand lhsOperand = operands.get(0);
        assertEquals(OperandType.Reference, lhsOperand.type());
        assertEquals("0", lhsOperand.value());

        final Operand rhsOperand = operands.get(1);
        assertEquals(OperandType.Reference, rhsOperand.type());
        assertEquals("1", rhsOperand.value());
    }

    @Test
    void testFibonacci() {
        final Expression expr = expressionParser.parse(fibonacciSeries(10));

        final List<OperationInstance> instances = expressionBuilder.createStack(expr);
        assertEquals(177, instances.size());
    }

    @Test
    void testFunctionCall() {
        final Expression expr = expressionParser.parse("SIN(1)");

        final List<OperationInstance> instances = expressionBuilder.createStack(expr);
        assertEquals(2, instances.size());
    }

}