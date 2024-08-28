package com.ericsson.itpf.deployment.test.cases;

import com.ericsson.cifwk.taf.TestCase;
import com.ericsson.cifwk.taf.TorTestCaseHelper;
import com.ericsson.cifwk.taf.annotations.Context;
import com.ericsson.cifwk.taf.annotations.TestId;
import com.ericsson.cifwk.taf.data.Host;
import com.ericsson.cifwk.taf.guice.OperatorRegistry;
import com.ericsson.cifwk.taf.tools.cli.TimeoutException;
import com.ericsson.itpf.deployment.test.operators.DeploymentRuntimeOperator;
import com.ericsson.itpf.deployment.test.operators.ENMLitpOperator;
import com.ericsson.nms.host.HostConfigurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;
import org.xml.sax.SAXException;

import javax.inject.Inject;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;
import java.io.IOException;


public class VerifyBladesDeployed_FunctionalTest extends TorTestCaseHelper implements TestCase {

    private static final Logger logger = LoggerFactory.getLogger(VerifyBladesDeployed_FunctionalTest.class);

    @Inject
    private OperatorRegistry<DeploymentRuntimeOperator> deploymentRuntimeOperatorRegistry;

    @Inject
    private OperatorRegistry<ENMLitpOperator> enmLitpOperatorRegistry;

    /**
     * @DESCRIPTION Verify that the correct number of blades are deployed
     * @PRE ENMInst deployed, access to the ms
     * @PRIORITY HIGH
     */
    @TestId(id = "TORF-21628_VerifyCorrectNumberOfBladesDeployed_ENMDeployed_Success", title = "Verify that the correct number of blades are deployed")
    @Context(context = {Context.CLI})
    @Test
    public void verifyCorrectNumberOfBladesDeployed() throws InterruptedException, TimeoutException, IOException,
            ParserConfigurationException, SAXException, XPathExpressionException {

        ENMLitpOperator emnLitpOperator = enmLitpOperatorRegistry.provide(ENMLitpOperator.class);

        int nodesFromLitp = emnLitpOperator.getAllPeerNodes().size();

        DeploymentRuntimeOperator deploymentRuntimeOperator = deploymentRuntimeOperatorRegistry.
                provide(DeploymentRuntimeOperator.class);

        Host host = HostConfigurator.getMS();
        deploymentRuntimeOperator.parseTags(host);
        assertEquals("Number of blades in LITP Model different then in XML Runtime Deployment", nodesFromLitp, deploymentRuntimeOperator.getNodesFromXML());
    }
}
