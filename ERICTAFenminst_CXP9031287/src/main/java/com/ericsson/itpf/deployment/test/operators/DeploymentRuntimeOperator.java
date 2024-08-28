package com.ericsson.itpf.deployment.test.operators;

import com.ericsson.cifwk.taf.data.Host;

public interface DeploymentRuntimeOperator {

    int getNodesFromXML();

    void parseTags(Host host);
}
