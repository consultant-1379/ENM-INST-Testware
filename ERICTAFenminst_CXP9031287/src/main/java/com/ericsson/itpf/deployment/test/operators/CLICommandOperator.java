package com.ericsson.itpf.deployment.test.operators;

import com.ericsson.cifwk.taf.data.Host;
import com.ericsson.cifwk.taf.data.User;
import com.ericsson.cifwk.taf.tools.cli.CLICommandHelper;

public interface CLICommandOperator {

    public User getUser(Host host, String username);

    public CLICommandHelper getCLICommandHelper(Host host);

    public CLICommandHelper getCLICommandHelper(String hostname);

    public CLICommandHelper getCLICommandHelper(String hostname, User user);

    public void clearCache();
}
