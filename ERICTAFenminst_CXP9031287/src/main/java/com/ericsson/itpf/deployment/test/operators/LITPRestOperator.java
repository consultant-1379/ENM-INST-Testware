package com.ericsson.itpf.deployment.test.operators;

import java.util.List;
import java.util.Map;

public interface LITPRestOperator {

    public Map<String, Object> getMap(String nodePath);

    public List<String> getChildren(String nodePath);

    public Map<String, String> getNodeProperties(String nodePath);

}
