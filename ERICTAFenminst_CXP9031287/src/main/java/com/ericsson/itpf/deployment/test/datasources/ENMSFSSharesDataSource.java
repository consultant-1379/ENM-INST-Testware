package com.ericsson.itpf.deployment.test.datasources;

import com.ericsson.cifwk.taf.annotations.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

import static com.ericsson.itpf.deployment.test.datasources.ENMDataSourceConst.*;

public class ENMSFSSharesDataSource extends ENMBaseDataSource {

    private static final Logger logger = LoggerFactory.getLogger(ENMSFSSharesDataSource.class);

    @DataSource
    public List<Map<String, Object>> dataSource() {

        List results = new ArrayList<>();

        Map<String, Set<String>> mountToHostsMap = new LinkedHashMap<>();

        Map<String, List<String>> hostToMountPointsMap = enmLitpOperator.getHostToMountPointsMap();
        for (String node : hostToMountPointsMap.keySet()) {
            List<String> mountPoints = hostToMountPointsMap.get(node);
            for (String mountPoint : mountPoints) {
                Set<String> hosts = mountToHostsMap.get(mountPoint);
                if (null == hosts) {
                    hosts = new LinkedHashSet<>();
                    mountToHostsMap.put(mountPoint, hosts);
                }
                if (!hosts.contains(node)) {
                    hosts.add(node);
                }
            }
        }

        logger.debug("mountToHostsMap {}", mountToHostsMap);

        for (String mountPoint : mountToHostsMap.keySet()) {
            Set<String> hosts = mountToHostsMap.get(mountPoint);
            Map<String, Object> entry = new HashMap<>();
            entry.put(DATA_LABEL_HOSTS, hosts.toArray());
            entry.put(DATA_LABEL_MOUNT, mountPoint);
            entry.put(DATA_LABEL_EXPECTED_EXIT_CODE, EXIT_CODE_OK);
            results.add(entry);
        }
        logger.debug("results {}", results);
        return results;
    }
}
