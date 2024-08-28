package com.ericsson.itpf.deployment.test.datasources;

import com.ericsson.cifwk.taf.annotations.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.ericsson.itpf.deployment.test.datasources.ENMDataSourceConst.*;

public class ENMSFSMountsDataSource extends ENMBaseDataSource {

    private static final Logger logger = LoggerFactory.getLogger(ENMSFSMountsDataSource.class);

    @DataSource
    public List<Map<String, Object>> dataSource() {

        List results = new ArrayList<>();

        Map<String, List<String>> hostToMountPointsMap = enmLitpOperator.getHostToMountPointsMap();
        for (String node : hostToMountPointsMap.keySet()) {
            List<String> mountPoints = hostToMountPointsMap.get(node);
            for (String mountPoint : mountPoints) {
                Map<String, Object> entry = new HashMap<>();
                entry.put(DATA_LABEL_HOST, node);
                entry.put(DATA_LABEL_MOUNT, mountPoint);
                entry.put(DATA_LABEL_EXPECTED_EXIT_CODE, EXIT_CODE_OK);
                results.add(entry);
            }
        }

        logger.debug("results {}", results);
        return results;
    }

}
