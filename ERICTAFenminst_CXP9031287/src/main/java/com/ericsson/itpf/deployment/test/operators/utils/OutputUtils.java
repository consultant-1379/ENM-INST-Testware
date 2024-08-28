package com.ericsson.itpf.deployment.test.operators.utils;

import java.util.Arrays;
import java.util.List;

public class OutputUtils {

    public static String getFirstLine(String output) {
        String line = output.split("\\r?\\n")[0].trim();
        return line;
    }

    public static List<String> parseOutputToLines(String output) {
        String lines[] = output.split("\\r?\\n");
        List<String> outputLines = Arrays.asList(lines);
        return outputLines;
    }
}
