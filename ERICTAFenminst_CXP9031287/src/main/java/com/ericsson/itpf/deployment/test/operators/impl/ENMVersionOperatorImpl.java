package com.ericsson.itpf.deployment.test.operators.impl;

import com.ericsson.cifwk.taf.TestContext;
import com.ericsson.cifwk.taf.annotations.Context;
import com.ericsson.cifwk.taf.annotations.Operator;
import com.ericsson.cifwk.taf.data.DataHandler;
import com.ericsson.cifwk.taf.data.Host;
import com.ericsson.cifwk.taf.guice.OperatorRegistry;
import com.ericsson.cifwk.taf.tools.cli.CLI;
import com.ericsson.cifwk.taf.tools.cli.CLICommandHelper;
import com.ericsson.cifwk.taf.tools.cli.Shell;
import com.ericsson.cifwk.taf.tools.cli.Terminal;
import com.ericsson.itpf.deployment.test.operators.CLICommandOperator;
import com.ericsson.itpf.deployment.test.operators.ENMOperatorsConst;
import com.ericsson.itpf.deployment.test.operators.ENMVersionOperator;
import com.ericsson.itpf.deployment.test.operators.utils.OutputUtils;
import com.ericsson.nms.host.HostConfigurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.ericsson.jcat.fw.logging.JcatLoggingApi;

import javax.inject.Inject;
import java.util.List;

@Operator(context = Context.CLI)
public class ENMVersionOperatorImpl implements ENMVersionOperator {

    private static final Logger logger = LoggerFactory.getLogger(ENMVersionOperatorImpl.class);


    @Inject
    private TestContext context;

    @Override
    public int executeCommand() {
        Host host = HostConfigurator.getMS();
        CLI cli = new CLI(host);
        String actualCommand = (String) DataHandler.getAttribute(ENMOperatorsConst.COMMAND_ALIAS_ENM_VERSION);
        JcatLoggingApi.setTestInfo("Performing '" + actualCommand + "' command on " + host.getHostname());
        Shell shell = null;
        try {
            shell = cli.executeCommand(Terminal.VT100, actualCommand);
            context.setAttribute(ENMOperatorsConst.CTX_ATTR_ACTUAL_COMMAND, actualCommand);
            String output = shell.read(ENMOperatorsConst.EXECUTION_TIME_OUT_SECONDS);
            logger.debug(output);
            List<String> outputLines = OutputUtils.parseOutputToLines(output);
            context.setAttribute(ENMOperatorsConst.CTX_ATTR_OUTPUT_LINES, outputLines);
            return shell.getExitValue();
        } finally {
            if (null != shell) {
                shell.disconnect();
            }
        }
    }

    public boolean matchLine(List<String> lines, String label, String regexp) {

        for (String line : lines) {
            if (!line.contains(label)) {
                continue;
            }
            logger.debug("line \'{}\' contains label \'{}\'", line, label);
            if (null == regexp) {
                return true;
            }
            if (line.matches(regexp)) {
                logger.debug("matched line \'{}\' with regexp \'{}\'", line, regexp);
                return true;
            }
        }
        return false;
    }

}
