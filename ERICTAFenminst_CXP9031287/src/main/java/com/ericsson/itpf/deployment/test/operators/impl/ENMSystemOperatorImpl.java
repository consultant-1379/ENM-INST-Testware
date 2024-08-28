package com.ericsson.itpf.deployment.test.operators.impl;

import com.ericsson.cifwk.taf.annotations.Context;
import com.ericsson.cifwk.taf.annotations.Operator;
import com.ericsson.cifwk.taf.data.Host;
import com.ericsson.cifwk.taf.guice.OperatorRegistry;
import com.ericsson.cifwk.taf.tools.cli.CLICommandHelper;
import com.ericsson.itpf.deployment.test.operators.CLICommandOperator;
import com.ericsson.itpf.deployment.test.operators.ENMSystemOperator;
import com.ericsson.itpf.deployment.test.operators.utils.OutputUtils;
import com.ericsson.nms.host.HostConfigurator;

import javax.inject.Inject;

@Operator(context = {Context.UNKNOWN, Context.CLI})
public class ENMSystemOperatorImpl implements ENMSystemOperator {

    private static final String TEMPLATE_CMD_GET_FILE_SIZE = "/usr/bin/stat -c\"%%s\" %s";

    @Inject
    private OperatorRegistry<CLICommandOperator> cliCmdOperatorRegistry;

    public long getFileSize(Host host,String filename) {

        CLICommandOperator cliCommandOperator = cliCmdOperatorRegistry
                .provide(CLICommandOperator.class);

        CLICommandHelper cliCommandHelper = cliCommandOperator
                .getCLICommandHelper(host);

        String cmdGetFileSize = String.format(TEMPLATE_CMD_GET_FILE_SIZE,filename);
        String output = cliCommandHelper.simpleExec(cmdGetFileSize);

        String text = OutputUtils.getFirstLine(output);
        return Integer.parseInt(text);
    }

}
