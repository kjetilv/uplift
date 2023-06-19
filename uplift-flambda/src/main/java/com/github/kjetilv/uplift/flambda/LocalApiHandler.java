package com.github.kjetilv.uplift.flambda;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import com.github.kjetilv.uplift.asynchttp.HttpChannelHandler;
import com.github.kjetilv.uplift.asynchttp.HttpReq;
import com.github.kjetilv.uplift.asynchttp.HttpRes;
import com.github.kjetilv.uplift.kernel.io.CaseInsensitiveHashMap;

final class LocalApiHandler implements HttpChannelHandler.Server {

    private final LocalLambdaHandler handler;

    private final HttpRes corsResponse;

    private final Map<String, List<String>> corsHeaders;

    LocalApiHandler(LocalLambdaHandler handler, CorsSettings corsSettings) {
        Objects.requireNonNull(corsSettings, "corsSettings");
        this.corsHeaders = CaseInsensitiveHashMap.wrap(Map.of(
            "Access-Control-Allow-Origin", List.of(corsSettings.originValue()),
            "Access-Control-Allow-Methods", List.of(corsSettings.methodsValue()),
            "Access-Control-Allow-Headers", List.of(corsSettings.headersValue()),
            "Access-Control-Max-Age", List.of("86400"),
            "Access-Control-Allow-Credentials", List.of(corsSettings.credentialsValue())
        ));
        this.corsResponse = new HttpRes(OK, corsHeaders, null);
        this.handler = Objects.requireNonNull(handler, "handler");
    }

    @Override
    public HttpRes handle(HttpReq req) {
        if (req.isCORS()) {
            return corsResponse;
        }
        LambdaResponse lambdaResponse = handler.lambdaResponse(new LambdaRequest(req));
        return lambdaResponse.toHttpResponse().updateHeaders(this::withCors);
    }

    private Map<String, List<String>> withCors(Map<String, ? extends List<String>> headers) {
        if (headers == null || headers.isEmpty()) {
            return corsHeaders;
        }
        Map<String, List<String>> combined = new HashMap<>(corsHeaders);
        combined.putAll(headers);
        return CaseInsensitiveHashMap.wrap(combined);
    }

    private static final int OK = 200;

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[" + handler + ", cors=" + corsResponse + "]";
    }
}
