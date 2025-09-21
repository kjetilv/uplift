package com.github.kjetilv.uplift.s3.auth;

import module java.base;

class AbstractSigner {

    static String formatDateTime(TemporalAccessor now) {
        return AwsAuths.DATETIME_FORMATTER.format(now);
    }

    static String formatDatestamp(TemporalAccessor now) {
        return AwsAuths.DATESTAMP_FORMATTER.format(now);
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

    protected static ZonedDateTime now() {
        return Instant.now().atZone(AwsAuths.UTC);
    }
}
