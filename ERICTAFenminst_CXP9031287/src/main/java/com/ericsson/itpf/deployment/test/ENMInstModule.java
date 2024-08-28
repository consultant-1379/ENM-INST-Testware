package com.ericsson.itpf.deployment.test;

import com.ericsson.itpf.deployment.test.operators.ENMLitpOperator;
import com.ericsson.itpf.deployment.test.operators.LITPRestOperator;
import com.ericsson.itpf.deployment.test.operators.impl.ENMLitpOperatorImpl;
import com.ericsson.itpf.deployment.test.operators.impl.LITPRestOperatorImpl;
import com.google.inject.Binder;
import com.google.inject.Module;

public class ENMInstModule implements Module {
    @Override
    public void configure(Binder binder) {
        binder.bind(LITPRestOperator.class).to(LITPRestOperatorImpl.class);
        binder.bind(ENMLitpOperator.class).to(ENMLitpOperatorImpl.class);
    }
}
