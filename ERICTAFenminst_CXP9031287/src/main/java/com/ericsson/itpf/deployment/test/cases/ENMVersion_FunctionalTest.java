package com.ericsson.itpf.deployment.test.cases;

import com.ericsson.cifwk.taf.TestCase;
import com.ericsson.cifwk.taf.TestContext;
import com.ericsson.cifwk.taf.TorTestCaseHelper;
import com.ericsson.cifwk.taf.annotations.Context;
import com.ericsson.cifwk.taf.annotations.Input;
import com.ericsson.cifwk.taf.annotations.TestId;
import com.ericsson.cifwk.taf.annotations.TestStep;
import com.ericsson.cifwk.taf.guice.OperatorRegistry;
import com.ericsson.cifwk.taf.scenario.TestScenario;
import com.ericsson.cifwk.taf.scenario.TestScenarioRunner;
import com.ericsson.cifwk.taf.scenario.TestStepFlow;
import com.ericsson.itpf.deployment.test.operators.*;
import com.ericsson.nms.host.HostConfigurator;
import com.google.common.base.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.Test;

import javax.inject.Inject;
import java.util.List;

import static com.ericsson.cifwk.taf.scenario.TestScenarios.*;

public class ENMVersion_FunctionalTest extends TorTestCaseHelper implements TestCase {

    private static final Logger logger = LoggerFactory.getLogger(ENMVersion_FunctionalTest.class);

    @Inject
    private OperatorRegistry<ENMVersionOperator> enmVersionOperatorRegistry;

    @Inject
    private OperatorRegistry<ENMSystemOperator> enmSystemOperatorRegistry;

    @Inject
    private TestContext context;

    @Inject
    private OperatorRegistry<CLICommandOperator> cliCmdOperatorRegistry;

    @AfterSuite
    public void tearDown() {
        CLICommandOperator cliCmdOperator = cliCmdOperatorRegistry.provide(CLICommandOperator.class);
        cliCmdOperator.clearCache();
    }

    /**
     * @DESCRIPTION Verify that ENMVersion scripts runs on MS node correctly
     * and the output generated contains given labels and matches given regular expressions
     * @PRE ENMInst has been successfully deployed
     * @PRIORITY HIGH
     */
    @TestId(id = "TORF-71721_validateOutput",
            title = "Verify that enm_version.sh output contains all important labels and values")
    @Context(context = {Context.CLI})
    @Test
    public void verifyENMVersionOutput() {
        TestStepFlow runToolFlow = flow("Run enm_version.sh script")
                .addTestStep(annotatedMethod(this, "runTool"))
                .build();

        TestStepFlow verifyToolFlow = flow("Validate output")
                .addTestStep(annotatedMethod(this, "validateOutput"))
                .withDataSources(dataSource("ERICTAFenminst_CXP9031287.verify-tool-enm_version"))
                .build();

        TestScenario scenario = scenario("enm_version scenario")
                .addFlow(runToolFlow)
                .addFlow(verifyToolFlow)
                .build();

        TestScenarioRunner runner = runner().build();
        runner.start(scenario);
    }

    @TestStep(id = "runTool")
    public void runTool() {
        logger.info("executing runTool");

        ENMSystemOperator enmSystemOperator = enmSystemOperatorRegistry.provide(ENMSystemOperator.class);
        long logSizeBeforeInvocation = enmSystemOperator.getFileSize(HostConfigurator.getMS(), ENMSystemConst.PATH_ENMINST_LOG);

        ENMVersionOperator enmVersionOperator = enmVersionOperatorRegistry.provide(ENMVersionOperator.class);
        int exitCode = enmVersionOperator.executeCommand();
        assertEquals("Incorrect exit code", exitCode, 0);

        long logSizeAfterInvocation = enmSystemOperator.getFileSize(HostConfigurator.getMS(), ENMSystemConst.PATH_ENMINST_LOG);
        saveAssertEquals("ENMInst log has changed", logSizeBeforeInvocation, logSizeAfterInvocation);

    }

    @TestStep(id = "validateOutput")
    public void validateOutput(@Input("Label") String label, @Input("Regexp") String regexp) {
        logger.info("executing validateOutput for label '{}' and regexp '{}'", label, regexp);

        List<String> outputLines = context.getAttribute(ENMOperatorsConst.CTX_ATTR_OUTPUT_LINES);

        String actualCommand = context.getAttribute(ENMOperatorsConst.CTX_ATTR_ACTUAL_COMMAND);

        String reason = String.format("The output of the command \'%s\' does not contain the expected \'%s\'", actualCommand, label);
        if (!Strings.isNullOrEmpty(regexp)) {
            reason += String.format(" and do not match regexp \'%s\'", regexp);
        }

        ENMVersionOperator operator = enmVersionOperatorRegistry.provide(ENMVersionOperator.class);
        saveAssertTrue(reason, operator.matchLine(outputLines, label, regexp));

    }

}
