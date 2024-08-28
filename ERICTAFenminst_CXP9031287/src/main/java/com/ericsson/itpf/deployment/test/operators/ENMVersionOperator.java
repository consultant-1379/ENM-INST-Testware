package com.ericsson.itpf.deployment.test.operators;

import java.util.List;

public interface ENMVersionOperator {

        public int executeCommand();

        public boolean matchLine(List<String> lines, String label,
                String regexp);
}
