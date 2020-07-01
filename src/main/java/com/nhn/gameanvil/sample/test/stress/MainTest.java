package com.nhn.gameanvil.sample.test.stress;

import org.junit.runner.JUnitCore;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;

public class MainTest {

    public static void main(String[] args) {

        Result result = JUnitCore.runClasses(Stress.class);
        System.out.println();

        for (Failure failure : result.getFailures()) {
            System.out.println("main() junit failure: " + failure.getTrace());
        }
        System.out.println("main() closed. result: " + result.wasSuccessful());

        System.exit(0);
    }

}
