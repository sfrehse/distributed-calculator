package com.hivemq.mqtt.dcalc;

import com.hivemq.mqtt.dcalc.expression.*;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.TerminalNode;

import java.util.List;

public class Visitor extends com.hivemq.mqtt.dcalc.CalculatorBaseVisitor<Expression> {

    @Override
    public Expression visitProg(com.hivemq.mqtt.dcalc.CalculatorParser.ProgContext ctx) {
        final List<ParseTree> childs = ctx.children;

        if (childs.size() != 2) {
            return null;
        }

        final ParseTree expr = childs.get(0);
        return expr.accept(this);
    }

    @Override
    public Expression visitExpr(com.hivemq.mqtt.dcalc.CalculatorParser.ExprContext ctx) {
        if (ctx.getChildCount() == 3) {
            return handleBinaryOperator(ctx);
        } else if (ctx.getChildCount() == 1) {
            return handleTerminalCase(ctx);
        } else if (ctx.fnName != null) {
            return handleFunctionCall(ctx);
        }
        System.out.println("unhandled case " + ctx);

        throw new RuntimeException("Unsupported expression type: %s".formatted(ctx));
    }

    private Expression handleFunctionCall(com.hivemq.mqtt.dcalc.CalculatorParser.ExprContext ctx) {
        final String functionName = ctx.getChild(0).getText().toLowerCase();
        final List<Expression> arguments = ctx.arguments.stream().map(
                argument -> {
                    return argument.accept(this);
                }).toList();

        return new FunctionCall(functionName, arguments, ctx.getText());
    }

    private Expression handleTerminalCase(com.hivemq.mqtt.dcalc.CalculatorParser.ExprContext ctx) {
        assert ctx.getChildCount() == 1;
        final TerminalNode terminalNode = (TerminalNode) ctx.getChild(0);
        return new DoubleConstantNodeExpression(Double.parseDouble(terminalNode.toString()), ctx.getText());
    }

    private Expression handleBinaryOperator(com.hivemq.mqtt.dcalc.CalculatorParser.ExprContext ctx) {
        final ParseTree lhs = ctx.left;
        final ParseTree rhs = ctx.right;

        final OperatorType operator = handleOperatorNode(ctx.operator);

        return new ArithmeticBinaryExpression(
                lhs.accept(this), operator, rhs.accept(this), ctx.getText()
        );
    }

    private OperatorType handleOperatorNode(Token operator) {
        switch (operator.getType()) {
            case com.hivemq.mqtt.dcalc.CalculatorParser.PLUS:
                return OperatorType.PLUS;

            case com.hivemq.mqtt.dcalc.CalculatorParser.MINUS:
                return OperatorType.MINUS;

            case com.hivemq.mqtt.dcalc.CalculatorParser.MULTIPLY:
                return OperatorType.MULTIPLY;

            case com.hivemq.mqtt.dcalc.CalculatorParser.DIVIDE:
                return OperatorType.DIVIDE;

        }
        throw new RuntimeException("Unsupported operator: %s".formatted(operator));
    }


}
