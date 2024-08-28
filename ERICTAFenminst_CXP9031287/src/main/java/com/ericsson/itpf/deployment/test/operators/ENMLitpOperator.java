package com.ericsson.itpf.deployment.test.operators;

import java.util.List;
import java.util.Map;

public interface ENMLitpOperator {

    public List<String> getAllHostnames();

    public List<String> getAllPeerNodes();

    public List<String> getPeerNodes(String cluster);

    public List<String> getClusters();

    public List<String> getClusterServices(String cluster);

    public Map<String, List<String>> getHostToMountPointsMap();

    public Map<String, List<String>> getParallelServices();

    public Map<String, List<String>> getActivePassiveServices();

    public Map<String, List<String>> getParallelVms();

    public Map<String, List<String>> getActivePassiveVms();

}
