package com.ericsson.itpf.deployment.test.operators;

import com.ericsson.cifwk.taf.data.Host;

public interface HostResolverOperator {

    /**
     * Retrieve host based on provided alias
     * @param alias
     * @return host associated with alias or null
     */
    Host getHostByAlias(String alias);

    /**
     * Try to resolve host based on provided hostname
     * using all available methods and best effort
     * @param hostname
     * @return host associated with hostname
     * @throws IllegalArgumentException if hostname was not resolvable
     */
    Host resolveHost(String hostname);
}
