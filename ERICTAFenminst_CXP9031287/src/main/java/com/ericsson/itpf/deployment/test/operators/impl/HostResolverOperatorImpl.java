package com.ericsson.itpf.deployment.test.operators.impl;

import com.ericsson.cifwk.taf.data.*;
import com.ericsson.cifwk.taf.tools.cli.CLICommandHelper;
import com.ericsson.itpf.deployment.test.operators.HostResolverOperator;
import com.ericsson.itpf.deployment.test.operators.utils.OutputUtils;
import com.google.common.primitives.Ints;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * Retrieves hosts information using different sources of information.
 * The base information is used based on the file /etc/hosts located on MS node
 * Additional source of information is DMT
 */
public class HostResolverOperatorImpl implements HostResolverOperator {

    private static final Logger logger = LoggerFactory.getLogger(
            CLICommandOperatorImpl.class);

    public static final String COMMENT_PREFIX = "#";
    public static final String COMMAND_CAT_ETC_HOSTS = "/bin/cat /etc/hosts";
    public static final String USERNAME_LITP_ADMIN = "litp-admin";
    public static final String USERNAME_ROOT = "root";

    private Map<String, Host> hostToIpMap = new LinkedHashMap();

    public HostResolverOperatorImpl(CLICommandHelper remoteShell) {
        String output = remoteShell.simpleExec(COMMAND_CAT_ETC_HOSTS);

        List<String> lines = OutputUtils.parseOutputToLines(output);

        for (String line : lines) {
            StringTokenizer st = new StringTokenizer(line);
            String ipAddress = null;
            if (st.hasMoreElements()) {
                ipAddress = st.nextToken();
            }
            if (ipAddress != null) {
                if (ipAddress.startsWith(COMMENT_PREFIX)) {
                    continue;
                }
                while (st.hasMoreElements()) {
                    String alias = st.nextToken();
                    if (alias.startsWith(COMMENT_PREFIX)) {
                        break;
                    }
                    Host host = buildHost(ipAddress, alias);
                    hostToIpMap.put(alias, host);
                }
            }

        }
        logger.debug("hostToIpMap {}", new TreeMap<>(hostToIpMap));
    }

    private Host buildHost(String ipAddress, String alias) {

        Host.HostBuilder hostBuilder = Host.builder().withName(alias).withIp(ipAddress);

        User user = determineUser(alias);
        hostBuilder.withUser(user);

        HostType hostType = determineHostType(alias);
        hostBuilder.withType(hostType);

        return hostBuilder.build();
    }

    private HostType determineHostType(String alias) {
        HostType hostType = null;
        String hostnameToHostType = mapHostnameToHostType(alias);
        try {
            hostType = HostType.valueOf(hostnameToHostType);
        } catch (IllegalArgumentException iae) {
            hostType = HostType.UNKNOWN;
        }
        return hostType;
    }

    private User determineUser(String alias) {
        String username = null;
        String password = null;
        UserType userType = null;
        String[] parts = alias.split("-");
        if (2 == parts.length) {
            Integer index = Ints.tryParse(parts[1]);
            if (null != index) {
                username = USERNAME_LITP_ADMIN;
                password = DataHandler.getAttribute(USERNAME_LITP_ADMIN).toString();
                userType = UserType.OPER;
            }
        }
        if (null == username) {
            username = USERNAME_ROOT;
            password = DataHandler.getAttribute(USERNAME_ROOT).toString();
            userType = UserType.ADMIN;
        }
        return new User(username, password, userType);
    }

    public Host getHostByAlias(String alias) {
        Host host = hostToIpMap.get(alias);
        return host;
    }

    public Host resolveHost(String hostname) {
        logger.debug("resolve host using alias with original hostname {}", hostname);
        Host host = getHostByAlias(hostname);
        if (null == host) {
            String hostnameFixed = mapHostnameToDataHandlerHostname(hostname);
            logger.debug("resolve host using alias with fixed hostname {}", hostnameFixed);
            host = getHostByAlias(hostnameFixed);
            if (null == host) {
                logger.debug("resolve host using DataHandler with fixed hostname {}", hostnameFixed);
                host = DataHandler.getHostByName(hostnameFixed);
            }
            if (null == host) {
                logger.debug("resolve host using DataHandler with original hostname {}", hostnameFixed);
                host = DataHandler.getHostByName(hostname);
            }
            if (null == host) {
                throw new IllegalArgumentException("Could not find host for given hostname " + hostname);
            }
        }
        return host;
    }

    private String mapHostnameToDataHandlerHostname(String hostname) {
        return hostname.replace("-", "");
    }

    private String mapHostnameToHostType(String hostname) {
        String hostType = hostname.replace("-", "");
        hostType = hostType.replaceAll("\\d","");
        return hostType.toUpperCase();
    }

}
