package com.hivemq.mqtt.dcalc.engine;

import com.hivemq.mqtt.dcalc.expression.OperatorType;
import com.hivemq.mqtt.dcalc.runtime.model.Operand;
import com.hivemq.mqtt.dcalc.runtime.model.OperationInstance;
import com.hivemq.mqtt.dcalc.runtime.model.Program;

import java.util.List;

public class Evaluator {
    final List<OperatorType> functionCalls = List.of(OperatorType.SIN);

    public Program eval(final Program program) {
        assert program.pc() < program.instructions().size();
        final double result = evaluatePC(program.pc(), program.instructions());
        final OperationInstance currentOperations = program.instructions().get(program.pc());

        final List<OperationInstance> updatedInstructions = program.instructions();
        updatedInstructions.set(program.pc(), new OperationInstance(
                currentOperations.operatorType(),
                currentOperations.operands(),
                result
        ));

        return new Program(program.pc() + 1, updatedInstructions);
    }

    private double evaluatePC(int pc, List<OperationInstance> instructions) {
        final OperationInstance operationInstance = instructions.get(pc);

        if (operationInstance.operatorType() == OperatorType.IDENTITY) {
            return operationInstance.result();
        } else if (functionCalls.contains(operationInstance.operatorType())) {
            return handleFunctionCall(operationInstance, instructions);
        }

        final List<Operand> operands = operationInstance.operands();
        final double lhsValue = getMemoryValue(Integer.parseInt(operands.get(0).value()), instructions);
        final double rhsValue = getMemoryValue(Integer.parseInt(operands.get(1).value()), instructions);


        switch (operationInstance.operatorType()) {
            case PLUS -> {
                return lhsValue + rhsValue;
            }

            case MINUS -> {
                return lhsValue - rhsValue;
            }

            case MULTIPLY -> {
                return lhsValue * rhsValue;
            }

            case DIVIDE -> {
                return lhsValue / rhsValue;
            }
        }

        throw new IllegalStateException("This should not be reached.");
    }

    private double handleFunctionCall(OperationInstance operationInstance, List<OperationInstance> instructions) {
        final List<Double> values = operationInstance.operands().stream().map(
                operand -> getMemoryValue(operand.value(), instructions)
        ).toList();

        switch (operationInstance.operatorType()) {
            case SIN -> {
                return Math.sin(values.get(0));
            }
        }

        throw new RuntimeException("Unsupported function %s".formatted(operationInstance.operatorType()));
    }

    private double getMemoryValue(String address, List<OperationInstance> instructions) {
        return getMemoryValue(Integer.parseInt(address), instructions);
    }

    private double getMemoryValue(int address, List<OperationInstance> instructions) {
        final Double value = instructions.get(address).result();

        if (value == null) {
            throw new IllegalStateException("State shouldn't be reached since value isn't computed so far. ADDRESS=%d".formatted(address));
        }
        return value;
    }
}
