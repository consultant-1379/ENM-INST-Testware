package com.ericsson.itpf.deployment.test.operators;

public interface ENMInstallationDocumentOperator {

    String executeGetRequest(String site, String extension);

    int getExitCode(String site, String extension);

}
