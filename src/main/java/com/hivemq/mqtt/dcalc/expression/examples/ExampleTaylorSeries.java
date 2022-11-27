package com.hivemq.mqtt.dcalc.expression.examples;

public class ExampleTaylorSeries {

    public static String taylor(int n) {
        if (n == 0) {
            return "5";
        } else {
            return String.format("sin(%s)", taylor(n - 1));
        }
    }
}
