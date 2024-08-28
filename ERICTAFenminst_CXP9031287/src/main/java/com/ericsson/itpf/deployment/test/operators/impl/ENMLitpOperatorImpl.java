package com.ericsson.itpf.deployment.test.operators.impl;

import com.ericsson.cifwk.taf.annotations.Context;
import com.ericsson.cifwk.taf.annotations.Operator;
import com.ericsson.itpf.deployment.test.operators.ENMLitpOperator;
import com.ericsson.itpf.deployment.test.operators.LITPRestOperator;
import com.ericsson.nms.host.HostConfigurator;
import com.google.common.collect.ImmutableMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Provider;
import java.util.*;

@Operator(context = { Context.UNKNOWN,
        Context.CLI }) public class ENMLitpOperatorImpl
        implements ENMLitpOperator {

        private static final Logger logger = LoggerFactory
                .getLogger(ENMLitpOperatorImpl.class);

        public static final String PATH_DEPLOYMENTS_CLUSTERS = "/deployments/enm/clusters/";
        public static final String TEMPLATE_PATH_DEPLOYMENTS_NODES = "/deployments/enm/clusters/%s/nodes";

        public static final String TEMPLATE_PATH_NODE_FILESYSTEMS = "/deployments/enm/clusters/%s/nodes/%s/file_systems/";

        public static final String TEMPLATE_PATH_MS_FILESYSTEMS = "/ms/file_systems/";

        public static final String TEMPLATE_PATH_CLUSTER_SERVICES = "/deployments/enm/clusters/%s/services/";

        public static final String PATH_SOFTWARE_SERVICES = "/software/services/";

        public static final int SERVICE_PARALLEL_ATTRIBUTE_STANDBY = 0;

        public static final int SERVICE_ACTIVE_PASSIVE_ATTRIBUTE_STANDBY = 1;

        public static final String SERVICE_TYPE_VM_SERVICE = "vm-service";
        public static final String CLUSTERED_SERVICE_POSTFIX = "_clustered_service";
        public static final String PROPERTY_STANDBY = "standby";
        public static final String PROPERTY_NODE_LIST = "node_list";
        public static final String PROPERTY_MOUNT_POINT = "mount_point";
        public static final String PROPERTY_ITEM_TYPE_NAME = "item-type-name";

        public static Map<String, String> CLUSTER_SERVICE_2_SOFTWARE_SERVICE_MAP = ImmutableMap
                .of("jms_clustered_service", "jms_service",
                        "postgres_clustered_service", "postgresql",
                        "versant_clustered_service", "versant_service");

        @Inject private Provider<LITPRestOperatorImpl> provider;

        public List<String> getAllPeerNodes() {
                LITPRestOperatorImpl litpRestOperator = provider.get();
                List<String> clusters = getClusters();
                List<String> result = new ArrayList<>();

                for (String cluster : clusters) {
                        List<String> nodes = getPeerNodes(cluster);
                        result.addAll(nodes);
                }
                logger.debug("all nodes {}", result);
                return result;
        }

        public List<String> getAllHostnames() {
                List<String> result = getAllPeerNodes();
                result.add(HostConfigurator.getMS().getHostname());
                return result;
        }

        public List<String> getPeerNodes(String cluster) {
                LITPRestOperatorImpl litpRestOperator = provider.get();
                String pathNodes = String
                        .format(TEMPLATE_PATH_DEPLOYMENTS_NODES, cluster);
                List<String> nodes = litpRestOperator.getChildren(pathNodes);
                return nodes;
        }

        public List<String> getClusterServices(String cluster) {
                LITPRestOperatorImpl litpRestOperator = provider.get();
                String pathNodes = String
                        .format(TEMPLATE_PATH_CLUSTER_SERVICES, cluster);
                List<String> services = litpRestOperator.getChildren(pathNodes);
                return services;
        }

        public List<String> getClusters() {
                LITPRestOperatorImpl litpRestOperator = provider.get();
                List<String> clusters = litpRestOperator
                        .getChildren(PATH_DEPLOYMENTS_CLUSTERS);
                logger.debug("clusters {}", clusters);
                return clusters;
        }

        public Map<String, List<String>> getHostToMountPointsMap() {
                Map<String, List<String>> results = new LinkedHashMap<>();

                LITPRestOperatorImpl litpRestOperator = provider.get();

                List<String> clusters = getClusters();
                for (String cluster : clusters) {
                        List<String> nodes = getPeerNodes(cluster);
                        for (String node : nodes) {
                                String listFilesystemsPath = String
                                        .format(TEMPLATE_PATH_NODE_FILESYSTEMS,
                                                cluster, node);
                                List<String> mountPoints = addMountPoints(
                                        litpRestOperator, listFilesystemsPath);
                                results.put(node, mountPoints);
                        }
                }
                results.put(HostConfigurator.getMS().getHostname(),
                        addMountPoints(litpRestOperator,
                                TEMPLATE_PATH_MS_FILESYSTEMS));
                logger.debug("results {}", results);
                return results;
        }

        @Override public Map<String, List<String>> getParallelServices() {
                return getServicesByAttributeStandBy(
                        SERVICE_PARALLEL_ATTRIBUTE_STANDBY);
        }

        @Override public Map<String, List<String>> getParallelVms() {
                return getVMServicesByAttributeStandBy(
                        SERVICE_PARALLEL_ATTRIBUTE_STANDBY);
        }

        private Map<String, List<String>> getVMServicesByAttributeStandBy(
                int standBy) {
                Map<String, List<String>> serviceToVmListMap = getServicesByAttributeStandBy(
                        standBy);

                Iterator<String> serviceIterator = serviceToVmListMap.keySet()
                        .iterator();

                while (serviceIterator.hasNext()) {
                        String clusterService = serviceIterator.next();
                        String service = translateClusterServiceToSoftwareService(
                                clusterService);
                        String serviceType = getServiceType(service);
                        if (!SERVICE_TYPE_VM_SERVICE.equals(serviceType)) {
                                logger.debug(
                                        "removed not VM service {} due to invalid service type {}",
                                        clusterService, serviceType);
                                serviceIterator.remove();
                        }
                }
                return serviceToVmListMap;
        }

        private String translateClusterServiceToSoftwareService(
                String clusterService) {

                if (CLUSTER_SERVICE_2_SOFTWARE_SERVICE_MAP
                        .containsKey(clusterService)) {
                        return CLUSTER_SERVICE_2_SOFTWARE_SERVICE_MAP
                                .get(clusterService);
                } else {
                        String service = clusterService
                                .replace(CLUSTERED_SERVICE_POSTFIX, "");
                        return service;
                }

        }

        @Override public Map<String, List<String>> getActivePassiveVms() {
                return getVMServicesByAttributeStandBy(
                        SERVICE_ACTIVE_PASSIVE_ATTRIBUTE_STANDBY);
        }

        private Map<String, List<String>> getServicesByAttributeStandBy(
                int standBy) {
                Map<String, List<String>> results = new LinkedHashMap<>();

                LITPRestOperatorImpl litpRestOperator = provider.get();

                List<String> clusters = getClusters();
                for (String cluster : clusters) {

                        List<String> services = getClusterServices(cluster);
                        for (String service : services) {
                                String serviceDetailPath = String.format(
                                        TEMPLATE_PATH_CLUSTER_SERVICES, cluster)
                                        + service + "/";
                                Map<String, String> serviceProperties = litpRestOperator
                                        .getNodeProperties(serviceDetailPath);

                                int serviceStandBy = Integer.parseInt(
                                        serviceProperties
                                                .get(PROPERTY_STANDBY));
                                String nodes = serviceProperties
                                        .get(PROPERTY_NODE_LIST);
                                if (standBy == serviceStandBy) {
                                        results.put(service, Arrays.asList(
                                                nodes.split(",")));
                                }
                        }
                }
                logger.debug("results {}", results);
                return results;
        }

        @Override public Map<String, List<String>> getActivePassiveServices() {
                return getServicesByAttributeStandBy(
                        SERVICE_ACTIVE_PASSIVE_ATTRIBUTE_STANDBY);
        }

        private List<String> addMountPoints(LITPRestOperator litpRestOperator,
                String listFilesystemsPath) {
                List<String> results = new ArrayList<>();
                List<String> fileSystems = litpRestOperator
                        .getChildren(listFilesystemsPath);
                logger.debug("fileSystems {}", fileSystems);
                for (String filesystem : fileSystems) {
                        String filesystemDetailsPath =
                                listFilesystemsPath + filesystem + "/";
                        Map<String, String> filesystemProperties = litpRestOperator
                                .getNodeProperties(filesystemDetailsPath);
                        String mountPoint = filesystemProperties
                                .get(PROPERTY_MOUNT_POINT);
                        results.add(mountPoint);
                }

                return results;
        }

        protected String getServiceType(String service) {
                LITPRestOperatorImpl litpRestOperator = provider.get();
                String servicePath = PATH_SOFTWARE_SERVICES + service + "/";
                Map<String, Object> map = litpRestOperator.getMap(servicePath);
                String serviceType = map.get(PROPERTY_ITEM_TYPE_NAME)
                        .toString();
                return serviceType;
        }

}
