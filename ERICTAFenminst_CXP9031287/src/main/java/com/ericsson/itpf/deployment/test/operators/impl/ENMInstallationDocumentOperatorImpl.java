package com.ericsson.itpf.deployment.test.operators.impl;

import com.ericsson.cifwk.taf.annotations.Context;
import com.ericsson.cifwk.taf.annotations.Operator;
import com.ericsson.cifwk.taf.data.DataHandler;
import com.ericsson.cifwk.taf.data.Host;
import com.ericsson.cifwk.taf.tools.http.HttpResponse;
import com.ericsson.cifwk.taf.tools.http.HttpTool;
import com.ericsson.cifwk.taf.tools.http.HttpToolBuilder;
import com.ericsson.itpf.deployment.test.operators.ENMInstallationDocumentOperator;

@Operator(context = Context.REST)
public class ENMInstallationDocumentOperatorImpl implements ENMInstallationDocumentOperator {

    private HttpResponse response;

    private void initialize(String site, String extension) {

        final Host host = DataHandler.getHostByName(site);
        final HttpTool tool = HttpToolBuilder.newBuilder(host).useHttpsIfProvided(true).trustSslCertificates(true).build();

        final String username = "taf-user";
        final String password = "taf-user";
        response = tool.request().authenticate(username, password).get(extension);
    }

    @Override
    public String executeGetRequest(final String site, final String extension) {

        initialize(site, extension);
        return response.getBody();
    }

    @Override
    public int getExitCode(final String site, final String extension) {

        initialize(site, extension);
        final int resp = response.getResponseCode().getCode();
        System.out.println("Exit code is " + resp);
        return resp;
    }
}
