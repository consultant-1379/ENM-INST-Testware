package com.ericsson.itpf.deployment.test.cases;

import com.ericsson.cifwk.taf.TestCase;
import com.ericsson.cifwk.taf.TorTestCaseHelper;
import com.ericsson.cifwk.taf.annotations.*;
import com.ericsson.cifwk.taf.guice.OperatorRegistry;
import com.ericsson.itpf.deployment.test.operators.ENMInstallationDocumentOperator;
import org.testng.annotations.Test;

import javax.inject.Inject;

public class ENMInstallationDocument_FunctionalTest extends TorTestCaseHelper implements TestCase {

    @Inject
    private OperatorRegistry<ENMInstallationDocumentOperator> enmInstallationDocProvider;

    /**
     * @DESCRIPTION Ensure that the web page containing installation
     * instructions is present within the procedure
     * @PRE Access to confluence is available
     * @PRIORITY HIGH
     */
    @TestId(id = "TORF-21628_VerifyInstallationInstructionsExist_Success", title = "Verify site with installation instructions exists")
    @Context(context = {Context.REST})
    @Test
    @DataDriven(name = "ERICTAFenminst_CXP9031287.instructions-doc")
    public void verifyWebpageExist(@Input("Host") String host, @Input("Page") String page, @Output("ExitCode") String exitCode) {

        ENMInstallationDocumentOperator enmInstallationDocOperator = enmInstallationDocProvider.provide(ENMInstallationDocumentOperator.class);
        setTestInfo("Load the web page from host: " + host + " and check page: " + page);
        final int code = enmInstallationDocOperator.getExitCode(host, page);
        assertEquals(Integer.parseInt(exitCode), code);
    }

    /**
     * @DESCRIPTION Ensure that the content in the page containing installation
     * instructions is as expected
     * @PRE Access to confluence is available
     * @PRIORITY MEDIUM
     */
    @TestId(id = "TORF-21628_VerifyContentsOfInstallationInstructions_Success", title = "Verify webpage contents")
    @Context(context = {Context.REST})
    @Test
    @DataDriven(name = "ERICTAFenminst_CXP9031287.instructions-doc-contents")
    public void verifyWebpageContents(@Input("Host") String host, @Input("Page") String page, @Output("Content") String content) {

        ENMInstallationDocumentOperator enmInstallationDocumentOperator = enmInstallationDocProvider.provide(ENMInstallationDocumentOperator.class);
        setTestInfo("Load relevant " + page + " page and ensure " + content + " is present");
        final String result = enmInstallationDocumentOperator.executeGetRequest(host, page);
        assertTrue(result.contains(content));
    }
}
