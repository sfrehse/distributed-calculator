package com.hivemq.mqtt.dcalc.expression.examples;

import java.util.List;
import java.util.stream.IntStream;

public class Gauss {

    public static String generate(int n) {
        final List<String> values = IntStream.range(0, n).boxed().map(String::valueOf).toList();
        return String.join("+", values);
    }
}
