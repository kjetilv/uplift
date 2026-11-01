package com.github.kjetilv.uplift.s3.auth;

import module java.base;

class AbstractSigner {

    static String formatDateTime(Instant time) {
        return AwsAuths.DATETIME_FORMATTER.format(time.atZone(AwsAuths.UTC));
    }

    static String formatDatestamp(Instant time) {
        return AwsAuths.DATESTAMP_FORMATTER.format(time.atZone(AwsAuths.UTC));
    }

    private final URI endpointUrl;

    private final String httpMethod;

    private final String serviceName;

    private final String regionName;

    AbstractSigner(URI endpointUrl, String httpMethod, String serviceName, String regionName) {
        this.endpointUrl = endpointUrl;
        this.httpMethod = httpMethod;
        this.serviceName = serviceName;
        this.regionName = regionName;
    }

    URI getEndpointUrl() {
        return endpointUrl;
    }

    String getHttpMethod() {
        return httpMethod;
    }

    String getServiceName() {
        return serviceName;
    }

    String getRegionName() {
        return regionName;
    }
}
