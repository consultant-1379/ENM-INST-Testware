package com.ericsson.itpf.deployment.test.datasources;

import com.ericsson.cifwk.taf.annotations.DataSource;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.ericsson.itpf.deployment.test.datasources.ENMDataSourceConst.*;

public abstract class ENMVmBaseDataSource extends ENMBaseDataSource {

    @DataSource
    public List<Map<String, Object>> dataSource() {

        List results = new ArrayList<>();

        Map<String, List<String>> serviceToVmListMap = getVms();

        for (String service : serviceToVmListMap.keySet()) {
            List<String> nodes = serviceToVmListMap.get(service);
            Map<String, Object> entry = new HashMap<>();
            entry.put(DATA_LABEL_VM_NAME, service);
            entry.put(DATA_LABEL_NODE_NAME, nodes.toArray());
            entry.put(DATA_LABEL_STATE, VM_STATE_RUNNING);
            results.add(entry);
        }
        return results;
    }

    public abstract Map<String, List<String>> getVms();
}
