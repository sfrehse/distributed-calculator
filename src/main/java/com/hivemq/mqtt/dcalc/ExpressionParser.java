package com.hivemq.mqtt.dcalc;

import com.hivemq.mqtt.dcalc.expression.Expression;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;

public class ExpressionParser {

    public Expression parse(String expr) {
        final CharStream charStreams = CharStreams.fromString(expr);
        final com.hivemq.mqtt.dcalc.CalculatorLexer lexer = new com.hivemq.mqtt.dcalc.CalculatorLexer(charStreams);
        final CommonTokenStream tokens = new CommonTokenStream(lexer);
        final com.hivemq.mqtt.dcalc.CalculatorParser parser = new com.hivemq.mqtt.dcalc.CalculatorParser(tokens);

        final ParseTree parseTree = parser.prog();
        return parseTree.accept(new Visitor());
    }
}
