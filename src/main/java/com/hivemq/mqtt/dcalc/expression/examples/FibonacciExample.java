package com.hivemq.mqtt.dcalc.expression.examples;

public class FibonacciExample {
    static public String fibonacciSeries(int n) {
        if (n == 0) {
            return "0";
        } else if (n == 1) {
            return "1";
        } else {
            return String.format("%s + %s", fibonacciSeries(n - 1), fibonacciSeries(n - 2));
        }
    }

}
