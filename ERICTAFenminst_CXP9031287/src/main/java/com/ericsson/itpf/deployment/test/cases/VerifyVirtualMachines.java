package com.ericsson.itpf.deployment.test.cases;

import com.ericsson.cifwk.taf.TestCase;
import com.ericsson.cifwk.taf.TorTestCaseHelper;
import com.ericsson.cifwk.taf.annotations.*;
import com.ericsson.cifwk.taf.data.DataHandler;
import com.ericsson.cifwk.taf.data.User;
import com.ericsson.cifwk.taf.guice.OperatorRegistry;
import com.ericsson.cifwk.taf.tools.cli.CLICommandHelper;
import com.ericsson.itpf.deployment.test.operators.CLICommandOperator;
import com.ericsson.itpf.deployment.test.operators.ENMOperatorsConst;
import com.ericsson.nms.host.HostConfigurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Test;

import javax.inject.Inject;

public class VerifyVirtualMachines extends TorTestCaseHelper implements TestCase {

    private static final Logger logger = LoggerFactory.getLogger(VerifyVirtualMachines.class);

    @Inject
    private OperatorRegistry<CLICommandOperator> cliCmdOperatorRegistry;

    private String cmdHastatus;

    private String cmdVirsh;

    @BeforeSuite
    public void setUp() {
        cmdHastatus = (String) DataHandler.getAttribute(ENMOperatorsConst.COMMAND_ALIAS_HASTATUS);
        cmdVirsh = (String) DataHandler.getAttribute(ENMOperatorsConst.COMMAND_ALIAS_VIRSH);
    }

    @AfterSuite
    public void tearDown() {
        CLICommandOperator cliCmdOperator = cliCmdOperatorRegistry.provide(CLICommandOperator.class);
        cliCmdOperator.clearCache();
    }

    /**
     * @DESCRIPTION Verify that services for the Virtual Machines are ONLINE on
     * all svc nodes
     * @PRE ENM deployed
     * @PRIORITY HIGH
     */
    @TestId(id = "TORF-35738_VerifyParallelServices_ENMDeployed_success", title = "Verify that the services for the VMs are online on all svc nodes")
    @Context(context = {Context.CLI})
    @Test
    @DataDriven(name = "ERICTAFenminst_CXP9031287.verify-service-parallel")
    public void verifyParallelServices_ENMDeployed_online(@Input("ServiceName") String serviceName, @Input("NodeName") String[] nodeNames) {
        setTestInfo("Verifying " + serviceName + " service");
        String command = cmdHastatus + " -sum | grep " + serviceName;
        CLICommandHelper cmdHelper = assertHopCommandExecute(nodeNames[0], command);
        assertFalse("The service is not parallel.", cmdHelper.getStdOut().toLowerCase().contains("offline"));
    }

    /**
     * @DESCRIPTION Verify that Active/Passive services for the Virtual Machines
     * are ONLINE on one of the svc nodes
     * @PRE ENM deployed
     * @PRIORITY HIGH
     */
    @TestId(id = "TORF-35738_VerifyActivePassiveServices_ENMDeployed_success", title = "Verify that the services for the VMs are online on one svc node")
    @Context(context = {Context.CLI})
    @Test
    @DataDriven(name = "ERICTAFenminst_CXP9031287.verify-service-active-passive")
    public void verifyActivePassiveServices_ENMDeployed_online(@Input("ServiceName") String serviceName, @Input("NodeName") String[] nodeNames) {
        setTestInfo("Verifying " + serviceName + " service");

        String command = cmdHastatus + " -sum | grep " + serviceName;
        CLICommandHelper cmdHelper = assertHopCommandExecute(nodeNames[0], command);

        String output = cmdHelper.getStdOut();
        boolean serviceOnlineOnOneNode = output.toLowerCase().contains("offline") & output.toLowerCase().contains("online");
        assertTrue("Service " + serviceName + " is not online on one of the nodes, as expected.", serviceOnlineOnOneNode);
    }

    /**
     * @DESCRIPTION Verify that the Virtual Machines are running on all svc
     * nodes
     * @PRE ENM deployed
     * @PRIORITY HIGH
     */
    @TestId(id = "TORF-35738_VerifyVmRunningParallel_ENMDeployed_running", title = "Verify that the VMs are running on all svc nodes")
    @Context(context = {Context.CLI})
    @Test
    @DataDriven(name = "ERICTAFenminst_CXP9031287.verify-vm-running-parallel")
    public void verifyVmRunningParallel_ENMDeployed_running(@Input("vmName") String vmName, @Input("NodeName") String nodeName, @Output("State") String state) {
        setTestInfo("Verifying " + vmName + " VM on " + nodeName);
        String command = cmdVirsh + " list | grep " + vmName + " | awk -F ' ' '{print $3}'";
        CLICommandHelper cmdHelper = assertHopCommandExecute(nodeName, command);
        assertTrue("The " + vmName + " VM is not running.", cmdHelper.getStdOut().contains(state));
    }

    /**
     * @DESCRIPTION Verify that the Virtual Machines are running on one of the
     * svc nodes
     * @PRE ENM deployed
     * @PRIORITY HIGH
     */
    @TestId(id = "TORF-35738_VerifyVmRunningActivePassive_ENMDeployed_running", title = "Verify that the VMs are running on one svc node")
    @Context(context = {Context.CLI})
    @Test
    @DataDriven(name = "ERICTAFenminst_CXP9031287.verify-vm-running-active-passive")
    public void verifyVmRunningActivePassive_ENMDeployed_running(
            @Input("vmName") String vmName,
            @Input("NodeName") String[] nodeNames,
            @Output("State") String state) {
        boolean runningOnOneNode = false;
        for (String nodeName : nodeNames) {
            setTestInfo("Verifying " + vmName + " VM on " + nodeName);
            String command = cmdVirsh + " list | grep " + vmName + " | awk -F ' ' '{print $3}'";
            CLICommandHelper cmdHelper = assertHopCommandExecute(nodeName, command);
            // The following XOR operation returns "true" if the VM is running
            // only on one of the nodes
            runningOnOneNode = runningOnOneNode ^ (cmdHelper.getStdOut().contains(state));
        }
        assertTrue("The " + vmName + " VM is not running or it's not active-passive.", runningOnOneNode);
    }

    private CLICommandHelper assertHopCommandExecute(String nodeName, String command) {

        CLICommandOperator cliCmdOperator = cliCmdOperatorRegistry.provide(CLICommandOperator.class);

        User rootUser = cliCmdOperator.getUser(HostConfigurator.getMS(), ENMOperatorsConst.USERNAME_ROOT);
        CLICommandHelper cmdHelper = cliCmdOperator.getCLICommandHelper(nodeName, rootUser);
        cmdHelper.execute(command);
        int exitCode = cmdHelper.getCommandExitValue();
        String reason = String.format("Exit code for command \'%s\' is not zero (%d)", command, exitCode);
        assertEquals(reason, exitCode, 0);

        return cmdHelper;
    }


}
