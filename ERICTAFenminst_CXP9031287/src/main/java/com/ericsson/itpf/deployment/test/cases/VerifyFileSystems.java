package com.ericsson.itpf.deployment.test.cases;

import com.ericsson.cifwk.taf.TestCase;
import com.ericsson.cifwk.taf.TorTestCaseHelper;
import com.ericsson.cifwk.taf.annotations.*;
import com.ericsson.cifwk.taf.data.User;
import com.ericsson.cifwk.taf.guice.OperatorRegistry;
import com.ericsson.cifwk.taf.tools.cli.CLICommandHelper;
import com.ericsson.cifwk.taf.tools.cli.jsch.JSchCLIToolException;
import com.ericsson.itpf.deployment.test.operators.CLICommandOperator;
import com.ericsson.itpf.deployment.test.operators.ENMOperatorsConst;
import com.ericsson.nms.host.HostConfigurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.Test;

import javax.inject.Inject;

public class VerifyFileSystems extends TorTestCaseHelper implements TestCase {

    private static final Logger logger = LoggerFactory.getLogger(VerifyFileSystems.class);

    public static final String TESTFILE_TXT = "testfile.txt";

    @Inject
    private OperatorRegistry<CLICommandOperator> cliCmdOperatorRegistry;

    @AfterSuite
    public void tearDown() {
        CLICommandOperator cliCmdOperator = cliCmdOperatorRegistry.provide(CLICommandOperator.class);
        cliCmdOperator.clearCache();
    }

    /**
     * @DESCRIPTION Verify that the mount points have been created for the
     * shares on the appropriate nodes
     * @PRE Access to SFS, storage pool already created, same name as the one in
     * the SED
     * @PRIORITY HIGH
     */
    @TestId(id = "TORF-34447_VerifyMountPointsCreated_SFSonline_Success",
            title = "Verify that the mount points are created for the shares on the appropriate nodes")
    @Context(context = {Context.CLI})
    @Test
    @DataDriven(name = "ERICTAFenminst_CXP9031287.verify-sfs-mounts")
    public void verifyMountPointsCreated(@Input("Host") String hostname, @Input("Mount") String mount, @Output("ExpectedExitCode") int expectedExitCode) {

        CLICommandOperator cliCmdOperator = cliCmdOperatorRegistry.provide(CLICommandOperator.class);

        try {
            setTestInfo("Verifying " + mount + " on " + hostname);
            CLICommandHelper cmdHelper = cliCmdOperator.getCLICommandHelper(hostname);
            String command = "ls " + mount;
            cmdHelper.execute(command);
            assertEquals("The mount " + mount + " is not set.", expectedExitCode, cmdHelper.getCommandExitValue());
        } catch (JSchCLIToolException e) {
            setTestInfo("Failed login to " + hostname);
        }
    }


    /**
     * @DESCRIPTION Verify that the shares are working correctly. We will create
     * a file on one of the nodes and verify that it is accessible
     * from the others
     * @PRE Mount points correctly created on the nodes
     * @PRIORITY HIGH
     */
    @TestId(id = "TORF-34447_VerifyShareWorkingProperly_SFSonline_Success", title = "Verify that the shares are working correctly")
    @Context(context = {Context.CLI})
    @Test
    @DataDriven(name = "ERICTAFenminst_CXP9031287.verify-shares")
    public void verifyShareWorkingProperly(@Input("Mount") String mount, @Input("Hosts") String[] hosts, @Output("ExpectedExitCode") int expectedExitCode) {

        CLICommandOperator cliCmdOperator = cliCmdOperatorRegistry.provide(CLICommandOperator.class);

        String mainHost = hosts[0];
        User rootUser = cliCmdOperator.getUser(HostConfigurator.getMS(), ENMOperatorsConst.USERNAME_ROOT);
        CLICommandHelper mainCmdHelper = cliCmdOperator.getCLICommandHelper(mainHost, rootUser);
        mainCmdHelper.execute("cd " + mount);
        mainCmdHelper.execute("ls -al " + mount);
        if (mainCmdHelper.getStdOut().contains(TESTFILE_TXT)) {
            logger.debug("The test file {} already exists on '{}' in mounted directory '{}", TESTFILE_TXT, mainHost, mount);
        } else {
            mainCmdHelper.execute("touch " + TESTFILE_TXT);
            assertEquals("The test file was created successfully.", 0, mainCmdHelper.getCommandExitValue());
        }


        for (int i = 0; i < hosts.length - 1; i++) {
            String hostname = hosts[i + 1];
            CLICommandHelper cmdHelper = cliCmdOperator.getCLICommandHelper(hostname, rootUser);

            cmdHelper.execute("ls -al " + mount);
            assertTrue("The test file was not found on " + hostname + " in mounted directory", cmdHelper.getStdOut().contains(TESTFILE_TXT));
            logger.debug("The test file was found on '{}' in mounted directory '{}", hostname, mount);
        }

        mainCmdHelper.execute("cd " + mount);
        mainCmdHelper.execute("rm -f " + mount + "/" + TESTFILE_TXT);
        assertEquals("The test file was not removed successfully.", expectedExitCode, mainCmdHelper.getCommandExitValue());
    }
}
