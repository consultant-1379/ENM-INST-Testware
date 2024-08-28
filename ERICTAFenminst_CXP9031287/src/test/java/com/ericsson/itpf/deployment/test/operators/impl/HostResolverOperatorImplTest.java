package com.ericsson.itpf.deployment.test.operators.impl;

import com.ericsson.cifwk.taf.data.Host;
import com.ericsson.cifwk.taf.data.HostType;
import com.ericsson.cifwk.taf.tools.cli.CLICommandHelper;
import com.ericsson.itpf.deployment.test.operators.HostResolverOperator;
import com.google.common.io.Files;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Matchers;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.Charset;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.*;

public class HostResolverOperatorImplTest {

    private HostResolverOperator hostResolverOperator;

    @Before
    public void setUp() throws IOException {
        CLICommandHelper mockedCLICmdHelper = mock(CLICommandHelper.class);
        URL url = this.getClass().getResource("/_etc_hosts");
        File etcHostsFile = new File(url.getFile());
        String content = Files.toString(etcHostsFile, Charset.defaultCharset());
        when(mockedCLICmdHelper.simpleExec(Matchers.anyString())).thenReturn(content);

        hostResolverOperator = new HostResolverOperatorImpl(mockedCLICmdHelper);
    }

    @Test
    public void testGetHostByAlias() throws Exception {

        Host host = hostResolverOperator.getHostByAlias("svc-1");
        assertNotNull(host);
        assertNotNull(host.getIp());
    }

    @Test
    public void testGetHostByAliasMultipleAliases() throws Exception {
        Host localhost = hostResolverOperator.getHostByAlias("localhost");
        assertNotNull(localhost);
        assertNotNull(localhost.getIp());
        Host msHost = hostResolverOperator.getHostByAlias("ieatlms4405-1");
        assertNotNull(msHost);
        assertNotNull(msHost.getIp());
        assertEquals(localhost.getIp(), msHost.getIp(), "ip addresses should be the same");
    }

    @Test
    public void testGetHostByAliasNotFound() throws Exception {
        Host host = hostResolverOperator.getHostByAlias("unknown");
        assertNull(host);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testResolveHostNotFound() throws Exception {
        Host host = hostResolverOperator.resolveHost("unknown");
    }

    @Test
    public void testResolveHostAsPeerNode() throws Exception {

        Host host = hostResolverOperator.getHostByAlias("svc-1");
        verifyHost(host, HostType.SVC, HostResolverOperatorImpl.USERNAME_LITP_ADMIN);
    }

    private void verifyHost(Host host, HostType hostType, String user) {
        assertNotNull(host);
        assertNotNull(host.getIp());
        assertNotNull(host.getType());
        assertEquals(host.getType(), hostType);
        assertNotNull(host.getUser());
        assertEquals(host.getUser(), user);
        assertNotNull(host.getPass());
    }


    @Test
    public void testResolveHostAsVMNode() throws Exception {

        Host host = hostResolverOperator.getHostByAlias("svc-1-mspm");
        verifyHost(host, HostType.UNKNOWN, HostResolverOperatorImpl.USERNAME_ROOT);
    }


}