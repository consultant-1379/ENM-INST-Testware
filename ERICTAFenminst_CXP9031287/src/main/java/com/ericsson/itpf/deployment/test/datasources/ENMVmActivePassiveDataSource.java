package com.ericsson.itpf.deployment.test.datasources;

import com.ericsson.cifwk.taf.annotations.DataSource;

import java.util.List;
import java.util.Map;

public class ENMVmActivePassiveDataSource extends ENMVmBaseDataSource {

    /**
     * @TODO to be removed when CIS-16613 will be fixed
     * @return
     */
    @DataSource
    public List<Map<String, Object>> dataSource() {
        return super.dataSource();
    }


    @Override
    public Map<String, List<String>> getVms() {
        return enmLitpOperator.getActivePassiveVms();
    }
}
