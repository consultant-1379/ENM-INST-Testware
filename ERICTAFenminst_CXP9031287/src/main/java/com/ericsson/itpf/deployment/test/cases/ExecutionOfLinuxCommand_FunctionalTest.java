package com.ericsson.itpf.deployment.test.cases;

import com.ericsson.cifwk.taf.TestCase;
import com.ericsson.cifwk.taf.TorTestCaseHelper;
import com.ericsson.cifwk.taf.annotations.*;
import com.ericsson.cifwk.taf.data.DataHandler;
import com.ericsson.cifwk.taf.guice.OperatorRegistry;
import com.ericsson.cifwk.taf.tools.cli.CLICommandHelper;
import com.ericsson.itpf.deployment.test.operators.CLICommandOperator;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.Test;

import javax.inject.Inject;

public class ExecutionOfLinuxCommand_FunctionalTest extends TorTestCaseHelper implements TestCase {

    @Inject
    private OperatorRegistry<CLICommandOperator> cliCmdOperatorRegistry;

    @AfterSuite
    public void tearDown() {
        CLICommandOperator cliCmdOperator = cliCmdOperatorRegistry.provide(CLICommandOperator.class);
        cliCmdOperator.clearCache();
    }

    /**
     * @DESCRIPTION Verify that Linux commands can be run on the newly deployed
     * MS and nodes
     * @PRE ENMInst has been successfully deployed
     * @PRIORITY HIGH
     */
    @TestId(id = "TORF-21627_VerifyLinuxCommandsRunOnAllNodes_ENMDeployed_Success",
            title = "Verify that Linux commands can be performed on MS and nodes deployed")
    @Context(context = {Context.CLI})
    @Test
    @DataDriven(name = "ERICTAFenminst_CXP9031287.commands-execution")
    public void verifyCLICommandsCanBeExecutedOnEnvironment(
            @Input("Host") String hostname,
            @Input("Command") String command,
            @Input("Args") String args,
            @Output("ExpectedOutput") String expectedOutput) {

        CLICommandOperator cliCmdOperator = cliCmdOperatorRegistry.provide(CLICommandOperator.class);

        CLICommandHelper cliCmdHelper = cliCmdOperator.getCLICommandHelper(hostname);
        String actualCommand = (String) DataHandler.getAttribute(command);
        setTestInfo("Performing '" + actualCommand + "' command on " + hostname);
        cliCmdHelper.execute(actualCommand);
        String output = cliCmdHelper.getStdOut(5);
        assertEquals("Exit code indicates command failure", cliCmdHelper.getCommandExitValue(), 0);
        String reason = String.format("The output \'%s\' of the command \'%s\' does not contain the expected \'%s\'", output, actualCommand, expectedOutput);
        assertTrue(reason, output.contains(expectedOutput));
    }
}
