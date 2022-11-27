package com.hivemq.mqtt.dcalc.engine;

import com.hivemq.mqtt.dcalc.expression.*;
import com.hivemq.mqtt.dcalc.runtime.model.Operand;
import com.hivemq.mqtt.dcalc.runtime.model.OperandType;
import com.hivemq.mqtt.dcalc.runtime.model.OperationInstance;

import java.util.*;
import java.util.stream.Stream;

public class ExpressionBuilder {
    private final Map<String, OperatorType> embeddedFunctions = new HashMap<>() {{
        put("sin", OperatorType.SIN);
    }};

    public List<OperationInstance> createStack(final Expression expression) {
        if (expression instanceof ArithmeticBinaryExpression) {
            return handleArithmeticBinaryExpression((ArithmeticBinaryExpression) expression);
        } else if (expression instanceof ConstantNodeExpression) {
            return handleConstantNodeExpression((ConstantNodeExpression) expression);
        } else if (expression instanceof FunctionCall) {
            return handleFunctionCall((FunctionCall) expression);
        }

        throw new RuntimeException("Unsupported expression yet: %s".formatted(expression));
    }

    private List<OperationInstance> handleFunctionCall(FunctionCall expression) {
        final String functionName = expression.getFunctionName().toLowerCase();
        if (embeddedFunctions.containsKey(functionName)) {
            final OperatorType operatorType = embeddedFunctions.get(functionName);

            final List<List<OperationInstance>> listOfOperationLists =
                    expression.getArguments().stream().map(this::createStack).toList();

            final List<OperationInstance> collectedOperands = new LinkedList<>();

            final List<Operand> operands = listOfOperationLists.stream().map(operationInstances -> {
                collectedOperands.addAll(operationInstances);
                return new Operand(OperandType.Reference, String.valueOf(collectedOperands.size()-1));
            }).toList();

            collectedOperands.add(new OperationInstance(operatorType, operands, null));
            return collectedOperands;
        }

        throw new RuntimeException("Unsupported function %s".formatted(functionName));
    }

    private List<OperationInstance> handleConstantNodeExpression(ConstantNodeExpression expression) {
        assert expression instanceof DoubleConstantNodeExpression;
        final DoubleConstantNodeExpression doubleConstantNodeExpression = (DoubleConstantNodeExpression) expression;

        return List.of(new OperationInstance(OperatorType.IDENTITY, Collections.emptyList(), doubleConstantNodeExpression.getValue()));
    }

    private List<OperationInstance> handleArithmeticBinaryExpression(ArithmeticBinaryExpression expression) {
        List<OperationInstance> stack = createStack(expression.getLhs());
        final Operand lhsOperand = new Operand(
                OperandType.Reference,
                String.valueOf(stack.size() - 1)
        );

        stack = Stream.concat(stack.stream(), createStack(expression.getRhs()).stream()).toList();

        final Operand rhsOperand = new Operand(
                OperandType.Reference,
                String.valueOf(stack.size() - 1)
        );


        final List<Operand> operands = Arrays.asList(lhsOperand, rhsOperand);

        final OperationInstance resultInstance = new OperationInstance(
                expression.getOperator(),
                operands,
                null
        );

        return Stream.concat(stack.stream(), Stream.of(resultInstance)).toList();
    }
}
