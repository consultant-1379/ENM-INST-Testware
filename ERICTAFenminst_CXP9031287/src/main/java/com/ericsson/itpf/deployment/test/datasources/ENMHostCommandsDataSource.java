package com.ericsson.itpf.deployment.test.datasources;

import com.ericsson.cifwk.taf.annotations.DataSource;
import com.ericsson.nms.host.HostConfigurator;
import com.google.common.base.Charsets;
import com.google.common.io.Resources;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.supercsv.comment.CommentStartsWith;
import org.supercsv.io.CsvListReader;
import org.supercsv.io.ICsvListReader;
import org.supercsv.prefs.CsvPreference;

import java.io.IOException;
import java.net.URL;
import java.util.*;

import static com.ericsson.itpf.deployment.test.datasources.ENMDataSourceConst.*;

public class ENMHostCommandsDataSource extends ENMBaseDataSource {

    private static final Logger logger = LoggerFactory.getLogger(ENMHostCommandsDataSource.class);

    private static final CsvPreference STANDARD_SKIP_COMMENTS =
            new CsvPreference.Builder(CsvPreference.STANDARD_PREFERENCE).skipComments(new CommentStartsWith("#")).build();

    @DataSource
    public List<Map<String, Object>> dataSource() {

        List results = new ArrayList<>();
        List<Map<String, Object>> commands = readCommands("data/ERICTAFenminst_CXP9031287.commands-execution-template.csv");


        List<String> hostnames = enmLitpOperator.getAllHostnames();

        for (String host : hostnames) {

            String acceptedType = HOST_TYPE_PEER;
            if (host.equals(HostConfigurator.getMS().getHostname())) {
                acceptedType = HOST_TYPE_MS;
            }

            for (Map<String, Object> cmdEntry : commands) {
                String hostType = cmdEntry.get(DATA_LABEL_HOST_TYPE).toString();
                if (!hostType.equalsIgnoreCase(acceptedType)) {
                    continue;
                }
                Map<String, Object> entry = new HashMap<>();
                entry.put(DATA_LABEL_HOST, host);
                entry.putAll(cmdEntry);
                results.add(entry);
            }
        }
        logger.debug("results {}", results);
        return results;
    }

    protected List<Map<String, Object>> readCommands(String filename) {

        List results = new ArrayList<>();
        ICsvListReader listReader = null;
        try {
            URL url = Resources.getResource(filename);
            listReader = new CsvListReader(Resources.asCharSource(url, Charsets.UTF_8).openStream(), STANDARD_SKIP_COMMENTS);

            String[] headers = listReader.getHeader(true);// skip the header (can't be used with CsvListReader)
            logger.debug("headers {}", Arrays.asList(headers));

            List<String> data;
            while ((data = listReader.read()) != null) {
                Map<String, Object> entry = new HashMap<>();
                logger.debug(String.format("lineNo=%s, rowNo=%s, data=%s", listReader.getLineNumber(),
                        listReader.getRowNumber(), data));

                if (headers.length != data.size()) {
                    throw new IllegalArgumentException(String.format("Number of header columns %d do not match number of data columns %d", headers.length, data.size()));
                }

                for (int i = 0; i < data.size(); i++) {
                    entry.put(headers[i], data.get(i));
                }
                results.add(entry);
            }

        } catch (IOException ioe) {
            throw new RuntimeException("Problem reading file " + filename, ioe);
        } finally {
            if (listReader != null) {
                try {
                    listReader.close();
                } catch (IOException e) {
                    logger.debug("Problem closing reader", e);
                }
            }
        }
        return results;
    }
}
