// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0

package handler.utils;

import static java.util.Map.entry;

import java.lang.reflect.Field;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import handler.model.DescribeUserGroupsRequestData;
import handler.model.DescribeUsersRequestData;
import handler.model.FilterBooleanToken;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.NullValueInNestedPathException;
import org.springframework.beans.PropertyAccessorFactory;
import org.springframework.stereotype.Component;

import handler.exceptions.BadRequestException;
import handler.model.DescribeServersUIRequestData;
import handler.model.DescribeSessionsUIRequestData;
import handler.model.DescribeSessionTemplatesRequestData;
import handler.model.FilterAvailabilityToken;
import handler.model.FilterDateToken;
import handler.model.FilterNumberToken;
import handler.model.FilterOsFamilyToken;
import handler.model.FilterProtocolToken;
import handler.model.FilterStateToken;
import handler.model.FilterToken;
import handler.model.FilterTypeToken;
import handler.model.FilterUnavailabilityReasonToken;
import handler.model.KeyValuePair;

@Component
@Slf4j
public class Filter<T, U> {
    private static final Map<Class, Map<String, String[]>> FILTER_PROPERTIES = Map.ofEntries(
            entry(DescribeSessionsUIRequestData.class, Map.ofEntries(
                entry("sessionIds", new String[]{"id"}),
                entry("sessionNames", new String[]{"name"}),
                entry("owners", new String[]{"owner"}),
                entry("ips", new String[]{"server.ip"}),
                entry("types", new String[]{"type"}),
                entry("states", new String[]{"state"}),
                entry("hostnames", new String[]{"server.hostname"}),
                entry("creationTimes", new String[]{"creationTime"}),
                entry("lastDisconnectionTimes", new String[]{"lastDisconnectionTime"}),
                entry("numOfConnections", new String[]{"numOfConnections"}))),
            entry(DescribeServersUIRequestData.class, Map.ofEntries(
                entry("ids", new String[]{"id"}),
                entry("ips", new String[]{"ip"}),
                entry("hostnames", new String[]{"hostname"}),
                entry("endpointIpAddresses", new String[]{"endpoints", "ipAddress"}),
                entry("ports", new String[]{"endpoints", "port"}),
                entry("webUrlPaths", new String[]{"endpoints", "webUrlPath"}),
                entry("protocols", new String[]{"endpoints", "protocol"}),
                entry("defaultDnsNames", new String[]{"defaultDnsName"}),
                entry("versions", new String[]{"version"}),
                entry("sessionManagerAgentVersions", new String[]{"sessionManagerAgentVersion"}),
                entry("availabilities", new String[]{"availability"}),
                entry("unavailabilityReasons", new String[]{"unavailabilityReason"}),
                entry("consoleSessionCounts", new String[]{"consoleSessionCount"}),
                entry("virtualSessionCounts", new String[]{"virtualSessionCount"}),
                entry("osFamilies", new String[]{"host.os.family"}),
                entry("osNames", new String[]{"host.os.name"}),
                entry("osVersions", new String[]{"host.os.version"}),
                entry("osKernelVersions", new String[]{"host.os.kernelVersion"}),
                entry("osBuildNumbers", new String[]{"host.os.buildNumber"}),
                entry("memoryTotalBytes", new String[]{"host.memory.totalBytes"}),
                entry("memoryUsedBytes", new String[]{"host.memory.usedBytes"}),
                entry("swapTotalBytes", new String[]{"host.swap.totalBytes"}),
                entry("swapUsedBytes", new String[]{"host.swap.usedBytes"}),
                entry("awsRegions", new String[]{"host.aws.region"}),
                entry("awsEc2InstanceTypes", new String[]{"host.aws.ec2InstanceType"}),
                entry("awsEc2InstanceIds", new String[]{"host.aws.ec2InstanceId"}),
                entry("awsEc2ImageIds", new String[]{"host.aws.ec2ImageId"}),
                entry("cpuVendors", new String[]{"host.cpuInfo.vendor"}),
                entry("cpuModelNames", new String[]{"host.cpuInfo.modelName"}),
                entry("cpuArchitectures", new String[]{"host.cpuInfo.architecture"}),
                entry("numberOfCpus", new String[]{"host.cpuInfo.numberOfCpus"}),
                entry("physicalCoresPerCpus", new String[]{"host.cpuInfo.physicalCoresPerCpu"}),
                entry("cpuLoadOneMinuteAverages", new String[]{"host.cpuLoadAverage.oneMinute"}),
                entry("cpuLoadFiveMinutesAverages", new String[]{"host.cpuLoadAverage.fiveMinutes"}),
                entry("cpuLoadFifteenMinutesAverages", new String[]{"host.cpuLoadAverage.fifteenMinutes"}),
                entry("gpuVendors", new String[]{"host.gpus", "vendor"}),
                entry("gpuModelName", new String[]{"host.gpus", "modelName"}),
                entry("loggedInUsers", new String[]{"host.loggedInUsers", "username"}),
                entry("tags", new String[]{"tags"})
            )),
            entry(DescribeSessionTemplatesRequestData.class, Map.ofEntries(
                entry("ids", new String[]{"id"}),
                entry("createdBy", new String[]{"createdBy"}),
                entry("creationTimes", new String[]{"creationTime"}),
                entry("names", new String[]{"name"}),
                entry("descriptions", new String[]{"description"}),
                entry("osFamilies", new String[]{"osFamily"}),
                entry("osVersions", new String[]{"osVersions"}),
                entry("instanceIds", new String[]{"instanceIds"}),
                entry("instanceTypes", new String[]{"instanceTypes"}),
                entry("instanceRegions", new String[]{"instanceRegions"}),
                entry("hostNumberOfCpus", new String[]{"hostNumberOfCpus"}),
                entry("hostMemoryTotalBytes", new String[]{"hostMemoryTotalBytes"}),
                entry("lastModifiedTimes", new String[]{"lastModifiedTime"}),
                entry("lastModifiedBy", new String[]{"lastModifiedBy"}),
                entry("types", new String[]{"type"}),
                entry("autorunFiles", new String[]{"autorunFile"}),
                entry("maxConcurrentClients", new String[]{"maxConcurrentClients"}),
                entry("initFiles", new String[]{"initFile"}),
                entry("storageRoots", new String[]{"storageRoot"}),
                entry("permissionsFiles", new String[]{"permissionsFile"}),
                entry("requirements", new String[]{"requirements"}),
                entry("autorunFileArguments", new String[]{"autorunFileArguments"}),
                entry("dcvGlEnabled", new String[]{"dcvGlEnabled"}),
                entry("enqueueRequest", new String[]{"enqueueRequest"}),
                entry("disableRetryOnFailure", new String[]{"disableRetryOnFailure"})
            )),
            entry(DescribeUsersRequestData.class, Map.ofEntries(
                    entry("userIds", new String[]{"userId"}),
                    entry("displayNames", new String[]{"displayName"}),
                    entry("roles", new String[]{"role"}),
                    entry("isDisabled", new String[]{"isDisabled"}),
                    entry("disabledReasons", new String[]{"disabledReason"}),
                    entry("isImported", new String[]{"isImported"}),
                    entry("creationTimes", new String[]{"creationTime"}),
                    entry("lastModifiedTimes", new String[]{"lastModifiedTime"}),
                    entry("lastLoggedInTimes", new String[]{"lastLoggedInTime"})
            )),
            entry(DescribeUserGroupsRequestData.class, Map.ofEntries(
                    entry("userGroupIds", new String[]{"userGroupId"}),
                    entry("displayNames", new String[]{"displayName"}),
                    entry("userIds", new String[]{"userId"}),
                    entry("isImported", new String[]{"isImported"}),
                    entry("creationTimes", new String[]{"creationTime"}),
                    entry("lastModifiedTimes", new String[]{"lastModifiedTime"})
            ))); // TODO: Add filter tokens for Sessions/Session Templates shared

    private boolean filter(Object value, Object filter) {
        if (filter instanceof FilterToken filterToken) {
            String propertyValue = (String) value;
            if (filterToken.getOperator() == FilterToken.OperatorEnum.EQUAL) {
                return filterToken.getValue().equals(propertyValue);
            }
            if (filterToken.getOperator() == FilterToken.OperatorEnum.NOT_EQUAL) {
                return !filterToken.getValue().equals(propertyValue);
            }
            if (filterToken.getOperator() == FilterToken.OperatorEnum.CONTAINS) {
                return propertyValue.contains(filterToken.getValue());
            }
            if (filterToken.getOperator() == FilterToken.OperatorEnum.NOT_CONTAINS) {
                return !propertyValue.contains(filterToken.getValue());
            }
            throw new RuntimeException("Failed to filter: FilterToken operator is invalid/null for " + filter);
        }
        if(filter instanceof FilterTypeToken filterTypeToken) {
            String propertyValue = (String) value;

            // Allow null operators for auto complete
            if (filterTypeToken.getOperator() == null) {
                return true;
            }
            if(filterTypeToken.getOperator() == FilterTypeToken.OperatorEnum.EQUAL) {
                return filterTypeToken.getValue().toString().equals(propertyValue);
            }
            if(filterTypeToken.getOperator() == FilterTypeToken.OperatorEnum.NOT_EQUAL) {
                return !filterTypeToken.getValue().toString().equals(propertyValue);
            }
        }
        if(filter instanceof FilterStateToken filterStateToken) {
            String propertyValue = (String) value;

            // Allow null operators for auto complete
            if (filterStateToken.getOperator() == null) {
                return true;
            }
            if(filterStateToken.getOperator() == FilterStateToken.OperatorEnum.EQUAL) {
                return filterStateToken.getValue().toString().equals(propertyValue);
            }
            if(filterStateToken.getOperator() == FilterStateToken.OperatorEnum.NOT_EQUAL) {
                return !filterStateToken.getValue().toString().equals(propertyValue);
            }
        }
        if(filter instanceof FilterNumberToken filterNumberToken) {
            Long propertyValue = ((Number) value).longValue();

            // Allow null operators for auto complete
            if (filterNumberToken.getOperator() == null) {
                return true;
            }
            if (filterNumberToken.getOperator() == FilterNumberToken.OperatorEnum.EQUAL) {
                return propertyValue.equals(filterNumberToken.getValue());
            }
            if (filterNumberToken.getOperator() == FilterNumberToken.OperatorEnum.NOT_EQUAL) {
                return !propertyValue.equals(filterNumberToken.getValue());
            }
            if (filterNumberToken.getOperator() == FilterNumberToken.OperatorEnum.LESS_THAN) {
                return propertyValue < filterNumberToken.getValue();
            }
            if (filterNumberToken.getOperator() == FilterNumberToken.OperatorEnum.LESS_THAN_OR_EQUAL_TO) {
                return propertyValue <= filterNumberToken.getValue();
            }
            if(filterNumberToken.getOperator() == FilterNumberToken.OperatorEnum.GREATER_THAN) {
                return propertyValue > filterNumberToken.getValue();
            }
            if(filterNumberToken.getOperator() == FilterNumberToken.OperatorEnum.GREATER_THAN_OR_EQUAL_TO) {
                return propertyValue >= filterNumberToken.getValue();
            }
        }
        if(filter instanceof FilterDateToken filterDateToken) {

            // Allow null operators for auto complete
            if (filterDateToken.getOperator() == null) {
                return true;
            }

            OffsetDateTime propertyValue = (OffsetDateTime) value;
            OffsetDateTime filterValue = OffsetDateTime.parse(filterDateToken.getValue());
            if(filterDateToken.getOperator() == FilterDateToken.OperatorEnum.EQUAL) {
                return propertyValue.isEqual(filterValue);
            }
            if(filterDateToken.getOperator() == FilterDateToken.OperatorEnum.NOT_EQUAL) {
                return !propertyValue.isEqual(filterValue);
            }
            if(filterDateToken.getOperator() == FilterDateToken.OperatorEnum.LESS_THAN) {
                return propertyValue.isBefore(filterValue);
            }
            if(filterDateToken.getOperator() == FilterDateToken.OperatorEnum.LESS_THAN_OR_EQUAL_TO) {
                return !propertyValue.isAfter(filterValue);
            }
            if(filterDateToken.getOperator() == FilterDateToken.OperatorEnum.GREATER_THAN) {
                return propertyValue.isAfter(filterValue);
            }
            if(filterDateToken.getOperator() == FilterDateToken.OperatorEnum.GREATER_THAN_OR_EQUAL_TO) {
                return !propertyValue.isBefore(filterValue);
            }
        }
        if(filter instanceof FilterProtocolToken filterProtocolToken) {
            String propertyValue = (String) value;

            // Allow null operators for auto complete
            if (filterProtocolToken.getOperator() == null) {
                return true;
            }
            if(filterProtocolToken.getOperator() == FilterProtocolToken.OperatorEnum.EQUAL) {
                return filterProtocolToken.getValue().toString().equals(propertyValue);
            }
            if(filterProtocolToken.getOperator() == FilterProtocolToken.OperatorEnum.NOT_EQUAL) {
                return !filterProtocolToken.getValue().toString().equals(propertyValue);
            }
        }
        if(filter instanceof FilterAvailabilityToken filterAvailabilityToken) {
            String propertyValue = (String) value;

            // Allow null operators for auto complete
            if (filterAvailabilityToken.getOperator() == null) {
                return true;
            }
            if(filterAvailabilityToken.getOperator() == FilterAvailabilityToken.OperatorEnum.EQUAL) {
                return filterAvailabilityToken.getValue().toString().equals(propertyValue);
            }
            if(filterAvailabilityToken.getOperator() == FilterAvailabilityToken.OperatorEnum.NOT_EQUAL) {
                return !filterAvailabilityToken.getValue().toString().equals(propertyValue);
            }
        }
        if(filter instanceof FilterUnavailabilityReasonToken filterUnavailabilityReasonToken) {
            String propertyValue = (String) value;

            // Allow null operators for auto complete
            if (filterUnavailabilityReasonToken.getOperator() == null) {
                return true;
            }
            if(filterUnavailabilityReasonToken.getOperator() == FilterUnavailabilityReasonToken.OperatorEnum.EQUAL) {
                return filterUnavailabilityReasonToken.getValue().toString().equals(propertyValue);
            }
            if(filterUnavailabilityReasonToken.getOperator() == FilterUnavailabilityReasonToken.OperatorEnum.NOT_EQUAL) {
                return !filterUnavailabilityReasonToken.getValue().toString().equals(propertyValue);
            }
        }
        if(filter instanceof FilterOsFamilyToken filterOsFamilyToken) {
            String propertyValue = (String) value;

            // Allow null operators for auto complete
            if (filterOsFamilyToken.getOperator() == null) {
                return true;
            }
            if(filterOsFamilyToken.getOperator() == FilterOsFamilyToken.OperatorEnum.EQUAL) {
                return filterOsFamilyToken.getValue().toString().equals(propertyValue);
            }
            if(filterOsFamilyToken.getOperator() == FilterOsFamilyToken.OperatorEnum.NOT_EQUAL) {
                return !filterOsFamilyToken.getValue().toString().equals(propertyValue);
            }
        }
        if(filter instanceof FilterBooleanToken filterBooleanToken) {
            Boolean propertyValue = (Boolean) value;

            // Allow null operators for auto complete
            if (filterBooleanToken.getOperator() == null) {
                return true;
            }
            if(FilterBooleanToken.OperatorEnum.EQUAL.equals(filterBooleanToken.getOperator())) {
                return filterBooleanToken.getValue().equals(propertyValue);
            }
            if(FilterBooleanToken.OperatorEnum.NOT_EQUAL.equals(filterBooleanToken.getOperator())) {
                return !filterBooleanToken.getValue().equals(propertyValue);
            }
        }
        if(filter instanceof KeyValuePair keyValuePair) {
            KeyValuePair propertyValue = (KeyValuePair) value;
            return keyValuePair.equals(propertyValue);
        }
        throw new UnsupportedOperationException("Failed to filter: Filtering not defined for " + filter);
    }

    private boolean isFiltered(U obj, String[] property, Object filter) {
        try {
            Object object = PropertyAccessorFactory.forBeanPropertyAccess(obj).getPropertyValue(property[0]);
            if(object == null) {
                return false;
            }
            if(object instanceof List objects) {
                for(Object objectProperty: objects) {
                    if(objectProperty != null) {
                        if(property.length > 1) {
                            Object objectValue = PropertyAccessorFactory.forBeanPropertyAccess(objectProperty).getPropertyValue(property[1]);
                            if(objectValue != null) {
                                if(filter(objectValue, filter)) {
                                    return true;
                                }
                            }
                        }
                        else if(filter(objectProperty, filter)) {
                            return true;
                        }
                    }
                }
                return false;
            }
            else {
                return filter(object, filter);
            }
        }
        catch(NullValueInNestedPathException e) {
            return false;
        }
    }

    private List<List<Predicate<U>>> getPredicateList(T request) {
        if(!FILTER_PROPERTIES.containsKey(request.getClass())) {
            throw new UnsupportedOperationException("Failed to filter: " + request.getClass() + " not in FILTER_PROPERTIES");
        }

        List<List<Predicate<U>>> predicates = new ArrayList<>();
        for(String filterProperty: FILTER_PROPERTIES.get(request.getClass()).keySet()) {
            List<Predicate<U>> propertyPredicates = new ArrayList<>();
            List<Object> tokens = (List<Object>) PropertyAccessorFactory.forBeanPropertyAccess(request).getPropertyValue(filterProperty);
            if(tokens != null) {
                for(Object filter: tokens) {
                    propertyPredicates.add(obj -> isFiltered(obj, FILTER_PROPERTIES.get(request.getClass()).get(filterProperty), filter));
                }
            }
            if(!propertyPredicates.isEmpty()) {
                predicates.add(propertyPredicates);
            }
        }
        return predicates;
    }

    public List<U> getFiltered(T request, List<U> list) {
        try {
            List<List<Predicate<U>>> predicates = getPredicateList(request);
            return list.stream()
                    .filter(predicates.stream().
                            map(l -> l.stream()
                                    .reduce(Predicate::or).orElse(x -> true))
                            .reduce(x -> true, Predicate::and))
                    .collect(Collectors.toList());
        }
        catch(Exception e) {
            throw new BadRequestException(e);
        }
    }
}
