package com.ericsson.itpf.deployment.test.datasources;

import com.ericsson.cifwk.taf.annotations.DataSource;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.ericsson.itpf.deployment.test.datasources.ENMDataSourceConst.*;

public abstract class ENMServicesBaseDataSource extends ENMBaseDataSource {

    @DataSource
    public List<Map<String, Object>> dataSource() {

        List results = new ArrayList<>();

        Map<String, List<String>> serviceToNodesListMap = getServices();

        for (String service : serviceToNodesListMap.keySet()) {
            List<String> nodes = serviceToNodesListMap.get(service);
            String vcsService = translateServiceToVcsServiceNamingConvention(service);
            Map<String, Object> entry = new HashMap<>();
            entry.put(DATA_LABEL_SERVICE_NAME, vcsService);
            entry.put(DATA_LABEL_NODE_NAME, nodes.toArray());
            entry.put(DATA_LABEL_EXPECTED_EXIT_CODE, EXIT_CODE_OK);
            results.add(entry);
        }
        return results;
    }

    /**
     * Translates service name to VCS naming convention
     * VCS services name do not support '-' character in the name
     * @param service name of the service
     * @return name translated to VCS convention
     */
    protected String translateServiceToVcsServiceNamingConvention(String service) {
        String vcsService = service;
        if (service.contains("-")) {
            vcsService=service.replaceAll("-","_");
        }
        return vcsService;
    }

    public abstract Map<String, List<String>> getServices();

}
