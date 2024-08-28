package com.ericsson.itpf.deployment.test.operators.impl;

import com.ericsson.cifwk.taf.annotations.Context;
import com.ericsson.cifwk.taf.annotations.Operator;
import com.ericsson.cifwk.taf.data.Host;
import com.ericsson.cifwk.taf.guice.OperatorRegistry;
import com.ericsson.cifwk.taf.tools.cli.CLICommandHelper;
import com.ericsson.itpf.deployment.test.operators.CLICommandOperator;
import com.ericsson.itpf.deployment.test.operators.DeploymentRuntimeOperator;
import com.ericsson.itpf.deployment.test.operators.utils.OutputUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;

@Operator(context = {Context.UNKNOWN, Context.CLI})
public class DeploymentRuntimeOperatorImpl implements DeploymentRuntimeOperator {

    private static final Logger logger = LoggerFactory.getLogger(DeploymentRuntimeOperatorImpl.class);

    public static final String ENMINST_DIR = "/opt/ericsson/enminst/runtime/";

    public static final String BLADE_XML = ENMINST_DIR + "enm_deployment.xml";

    public static final String TEMPLATE_COMMAND_GET_NUMBER_OF_NODES_FROM_XML_RUNTIME_DEPLOYMENT = "grep -o " + BLADE_XML + " --regexp \'%s\' | /usr/bin/wc -l";

    public static final String TAG_REGEXP_LITP_NODE = "litp\\:node\\s";

    @Inject
    private OperatorRegistry<CLICommandOperator> cliCmdOperatorRegistry;

    private int nodesFromXML;

    @Override
    public int getNodesFromXML() {
        return nodesFromXML;
    }

    @Override
    public void parseTags(Host host) {
        CLICommandOperator cliCmdOperator = cliCmdOperatorRegistry.provide(CLICommandOperator.class);

        CLICommandHelper cliCmdHelper = null;
        try {
            cliCmdHelper = cliCmdOperator.getCLICommandHelper(host.getHostname());
            String cmdGetNumberOfNodesFromXMLRuntimeDeployment = String.format(TEMPLATE_COMMAND_GET_NUMBER_OF_NODES_FROM_XML_RUNTIME_DEPLOYMENT, TAG_REGEXP_LITP_NODE);
            logger.info("Retrieving number of nodes from the xml file.");
            nodesFromXML = parseCommandOutputToInt(cliCmdHelper, cmdGetNumberOfNodesFromXMLRuntimeDeployment);
        } finally {
            if (null != cliCmdHelper) {
                cliCmdHelper.disconnect();
            }
        }
    }

    private int parseCommandOutputToInt(CLICommandHelper cliCmdHelper, String command) {
        cliCmdHelper.execute(command);
        String rawOutput = cliCmdHelper.getStdOut();
        String line = OutputUtils.getFirstLine(rawOutput);
        logger.debug("Output for command '{}' is '{}'", command, line);
        return Integer.parseInt(line);
    }

}
