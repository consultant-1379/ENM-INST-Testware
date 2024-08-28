package com.ericsson.itpf.deployment.test.operators.impl;

import com.ericsson.cifwk.taf.annotations.Context;
import com.ericsson.cifwk.taf.annotations.Operator;
import com.ericsson.cifwk.taf.data.Host;
import com.ericsson.cifwk.taf.data.User;
import com.ericsson.cifwk.taf.tools.cli.CLICommandHelper;
import com.ericsson.itpf.deployment.test.operators.CLICommandOperator;
import com.ericsson.itpf.deployment.test.operators.HostResolverOperator;
import com.ericsson.nms.host.HostConfigurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Singleton;
import java.util.LinkedHashMap;
import java.util.Map;

@Operator(context = {Context.UNKNOWN, Context.CLI})
@Singleton
public class CLICommandOperatorImpl implements CLICommandOperator {

    private static final Logger logger = LoggerFactory.getLogger(
            CLICommandOperatorImpl.class);

    private Map<String, CLICommandHelper> cache = new LinkedHashMap<>();

    private HostResolverOperator hostResolverOperator;

    public CLICommandOperatorImpl() {
        logger.debug("Initialize configuration");
        Host ms = HostConfigurator.getMS();
        logger.debug("Management Server IP is " + ms.getIp());
        hostResolverOperator = new HostResolverOperatorImpl(new CLICommandHelper(ms));
    }

    public CLICommandHelper getCLICommandHelper(Host host) {
        return getCLICommandHelper(host.getHostname(), null);
    }

    public CLICommandHelper getCLICommandHelper(String hostname) {
        return getCLICommandHelper(hostname, null);
    }

    public User getUser(Host host, String username) {
        for (User user : host.getUsers()) {
            if (user.getUsername().equals(username)) {
                return user;
            }
        }
        throw new IllegalArgumentException("User not found " + username + " on host " + host);
    }

    /**
     * Create instance of {@link CLICommandHelper} to be able to execute shell command on given hostname.
     * If hostname is different then MS node then hop is added which tunnel communication through MS node.
     * Creates instance if no cached instance for given hostname and user exists.
     * If instance exists and it is not closed then cached instance will be returned.
     *
     * @param hostname hostname to connect to
     * @param user     user to be used to execute the commands
     * @return instance of {@link CLICommandHelper}
     */
    public CLICommandHelper getCLICommandHelper(String hostname, User user) {

        String key = createKey(hostname, user);
        if (cache.containsKey(key)) {
            CLICommandHelper cliCmdHelper = cache.get(key);
            if (!cliCmdHelper.isClosed())
                return cliCmdHelper;
        }

        CLICommandHelper cmdHelper = null;
        Host host = hostResolverOperator.resolveHost(hostname);
        if (null == host.getUser()) {
            logger.debug("hostname {} with IP {} ", host.getHostname(), host.getIp());
        } else {
            logger.debug("hostname {} with IP {} using user {}", host.getHostname(), host.getIp(), host.getUser());
        }

        if (host.getHostname().equals("ms1")) {
            cmdHelper = new CLICommandHelper(host);
            if (null != user) {
                cmdHelper.newHopBuilder().hop(user).build();
            }
        } else {
            logger.debug("connect to '{}' through ms1 node", hostname);
            Host mainHost = HostConfigurator.getMS();
            cmdHelper = new CLICommandHelper(mainHost);
            if (null == user) {
                cmdHelper.newHopBuilder().hop(host).build();
            } else {
                cmdHelper = cmdHelper.newHopBuilder().hop(host).hop(user).build();
            }
        }
        cache.put(key, cmdHelper);
        return cmdHelper;
    }

    private String createKey(String hostname, User user) {
        StringBuilder sb = new StringBuilder();
        sb.append(hostname);
        if (null != user) {
            sb.append('-');
            sb.append(user.getUsername());
            sb.append('-');
            sb.append(user.getType());
        }
        return sb.toString();
    }

    /**
     * Clear internal cache. If any {@link CLICommandHelper} instance is cached then
     * {@link CLICommandHelper#disconnect()} method will called if instance is not closed yet.
     */
    public void clearCache() {
        for (CLICommandHelper cliCmdHelper : cache.values()) {
            if (!cliCmdHelper.isClosed()) {
                cliCmdHelper.disconnect();
            }
        }
        cache.clear();
    }

}
