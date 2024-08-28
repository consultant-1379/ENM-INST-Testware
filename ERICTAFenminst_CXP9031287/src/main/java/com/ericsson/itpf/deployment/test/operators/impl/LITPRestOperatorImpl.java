package com.ericsson.itpf.deployment.test.operators.impl;

import com.ericsson.cifwk.taf.annotations.Context;
import com.ericsson.cifwk.taf.annotations.Operator;
import com.ericsson.cifwk.taf.data.DataHandler;
import com.ericsson.cifwk.taf.data.Host;
import com.ericsson.cifwk.taf.data.Ports;
import com.ericsson.cifwk.taf.tools.http.HttpResponse;
import com.ericsson.cifwk.taf.tools.http.HttpTool;
import com.ericsson.cifwk.taf.tools.http.HttpToolBuilder;
import com.ericsson.cifwk.taf.tools.http.constants.HttpStatus;
import com.ericsson.itpf.deployment.test.operators.LITPRestOperator;
import com.ericsson.itpf.deployment.test.operators.LitpRestException;
import com.ericsson.nms.host.HostConfigurator;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

@Operator(context = {Context.REST, Context.CLI})
public class LITPRestOperatorImpl implements LITPRestOperator {

    public static final String LITP_REST_BASE_PATH = "/litp/rest/v1";
    public static final String LITP_RESPONSE_CHILD_KEY = "_embedded";
    public static final String LITP_RESPONSE_ITEM_KEY = "item";
    public static final String LITP_RESPONSE_ID_KEY = "id";
    public static final String LITP_RESPONSE_PROPERTIES_KEY = "properties";
    public static final String LITP_PORT_PROPERTY = "litp.port";
    public static final String LITP_USERNAME_PROPERTY = "litp.username";
    public static final String LITP_PASSWORD_PROPERTY = "litp.password";

    private static final Logger logger = LoggerFactory.getLogger(LITPRestOperatorImpl.class);

    private ObjectMapper objectMapper = new ObjectMapper();

    private static AtomicInteger requestsCounter = new AtomicInteger(0);

    private static Map<String, Map<String, Object>> getRequestCache = new HashMap<>();

    public Map<String, Object> getMap(String nodePath) {
        String url = LITP_REST_BASE_PATH + nodePath;
        Map<String, Object> map = getRequestCache.get(url);
        if (null == map) {
            String response = executeGetRequest(url);
            try {
                map = objectMapper.readValue(response, HashMap.class);
            } catch (IOException ioe) {
                throw new LitpRestException("Failed to read value for path " + nodePath, ioe);
            }
            getRequestCache.put(url, map);
        }
        return map;
    }

    @Override
    public List<String> getChildren(String nodePath) {
        logger.info("Retrieving sub-items under path {}", nodePath);
        List<String> children = new ArrayList<String>();
        Map<String, Object> map = getMap(nodePath);
        if (map.containsKey(LITP_RESPONSE_CHILD_KEY)) {
            List<Object> list = (List<Object>) ((Map<String, Object>) map.get(LITP_RESPONSE_CHILD_KEY))
                    .get(LITP_RESPONSE_ITEM_KEY);
            for (Object tmp : list) {
                children.add(((Map<String, Object>) tmp).get(LITP_RESPONSE_ID_KEY).toString());
            }
        } else {
            logger.info("No sub-items found under path {}", nodePath);
        }
        return children;
    }

    @Override
    public Map<String, String> getNodeProperties(String nodePath) {
        logger.info("Retrieving properties for path {}", nodePath);
        Map<String, Object> map = getMap(nodePath);
        Map<String, String> properties = new HashMap<String, String>();
        if (map.containsKey(LITP_RESPONSE_PROPERTIES_KEY)) {
            properties = (Map<String, String>) map.get(LITP_RESPONSE_PROPERTIES_KEY);
        } else {
            logger.info("No properties found for path {}", nodePath);
            properties = new HashMap<String, String>();
        }
        return properties;
    }

    private String executeGetRequest(String url) {
        logger.debug("request url {}", url);
        Host host = HostConfigurator.getMS();
        Map<Ports, String> portMap = new HashMap<>();
        String litpPort = DataHandler.getAttribute(LITP_PORT_PROPERTY).toString();
        portMap.put(Ports.HTTPS, litpPort);
        host.setPort(portMap);
        logger.debug("host ip "+host.getIp());
        String litpUsername = DataHandler.getAttribute(LITP_USERNAME_PROPERTY).toString();
        String litpPassword = DataHandler.getAttribute(LITP_PASSWORD_PROPERTY).toString();
        HttpTool tool = null;
        try {
            tool = HttpToolBuilder.newBuilder(host).useHttpsIfProvided(true).trustSslCertificates(true).build();
            HttpResponse httpResponse = tool.request().authenticate(litpUsername, litpPassword).get(url);
            int requests = requestsCounter.incrementAndGet();
            logger.debug("Total number of requests is {}",requests);

            String body = httpResponse.getBody();
            if (HttpStatus.OK.getCode() != httpResponse.getResponseCode().getCode()) {
                logger.debug("response body {} ", body);
                throw new LitpRestException("Incorrect HTTP Response Code " + httpResponse.getResponseCode());
            }
            return body;
        } finally {
            tool.close();
        }
    }
}