package com.ericsson.itpf.deployment.test.datasources;

import com.ericsson.itpf.deployment.test.ENMInstModule;
import com.ericsson.itpf.deployment.test.operators.ENMLitpOperator;
import com.google.inject.Guice;
import com.google.inject.Injector;

public abstract class ENMBaseDataSource {

    protected ENMLitpOperator enmLitpOperator;

    public ENMBaseDataSource() {
        Injector injector = Guice.createInjector(new ENMInstModule());

        enmLitpOperator = injector.getInstance(ENMLitpOperator.class);
    }
}

