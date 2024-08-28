package com.ericsson.itpf.deployment.test.cases;

import com.ericsson.cifwk.taf.TestCase;
import com.ericsson.cifwk.taf.TorTestCaseHelper;
import com.ericsson.cifwk.taf.annotations.*;
import com.ericsson.cifwk.taf.guice.OperatorRegistry;
import com.ericsson.itpf.deployment.test.operators.LITPRestOperator;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import org.testng.annotations.Test;

import javax.inject.Inject;
import java.io.IOException;
import java.util.Map;

public class VerifyPlanCompletedSuccessfully_FunctionalTest extends TorTestCaseHelper implements TestCase {

    @Inject
    private OperatorRegistry<LITPRestOperator> operatorRegistry;

    /**
     * @throws IOException
     * @throws JsonMappingException
     * @throws JsonParseException
     * @DESCRIPTION Verify that the LITP plan has completed successfully
     * @PRE ENMInst deployed, access to the MS
     * @PRIORITY HIGH
     */
    @TestId(id = "TORF-21627_VerifyLitpPlanCompletedSuccessfully_ENMDeployed_successful", title = "Verify LITP plan completed successfully")
    @Context(context = {Context.REST})
    @Test
    @DataDriven(name = "ERICTAFenminst_CXP9031287.verify_plan_completed")
    public void verifyPlanCompletedSuccessfully(@Input("NodePath") String nodePath, @Input("Property") String property,
                                                @Output("ExpectedOutput") String expectedOutput)
            throws JsonParseException, JsonMappingException, IOException {

        LITPRestOperator operator = operatorRegistry.provide(LITPRestOperator.class);
        Map<String, String> properties = null;
        properties = operator.getNodeProperties(nodePath);
        assertNotNull("Error retrieving plan details from LITP", properties);
        String output = properties.get(property);
        assertNotNull("LITP plan doesn't exist", output);
        assertEquals("LITP plan execution was not completed successfully", output, expectedOutput);
    }
}
