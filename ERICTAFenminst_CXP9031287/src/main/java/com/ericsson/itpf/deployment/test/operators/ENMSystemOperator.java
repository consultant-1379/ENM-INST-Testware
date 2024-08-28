package com.ericsson.itpf.deployment.test.operators;

import com.ericsson.cifwk.taf.data.Host;

public interface ENMSystemOperator {

    public long getFileSize(Host host, String filename);
}
