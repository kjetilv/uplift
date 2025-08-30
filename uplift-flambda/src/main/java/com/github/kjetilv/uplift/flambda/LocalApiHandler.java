package com.github.kjetilv.uplift.flambda;

import com.github.kjetilv.uplift.asynchttp.HttpChannelHandler;
import com.github.kjetilv.uplift.asynchttp.HttpReq;
import com.github.kjetilv.uplift.asynchttp.HttpRes;
import com.github.kjetilv.uplift.util.CaseInsensitiveHashMap;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

final class LocalApiHandler implements HttpChannelHandler.Server {

    private final LocalLambdaHandler handler;

    private final Map<String, List<String>> corsHeaders;

    LocalApiHandler(LocalLambdaHandler handler, CorsSettings cors) {
        Objects.requireNonNull(cors, "cors");
        this.corsHeaders = CaseInsensitiveHashMap.wrap(Map.of(
            "Access-Control-Allow-Origin", List.of(cors.originValue()),
            "Access-Control-Allow-Methods", List.of(cors.methodsValue()),
            "Access-Control-Allow-Headers", List.of(cors.headersValue()),
            "Access-Control-Max-Age", List.of("86400"),
            "Access-Control-Allow-Credentials", List.of(cors.credentialsValue())
        ));
        this.handler = Objects.requireNonNull(handler, "handler");
    }

    @Override
    public HttpRes handle(HttpReq req) {
        if (req.isCORS()) {
            return new HttpRes(OK, corsHeaders, null, req.id());
        }
        LambdaResponse lambdaResponse = handler.lambdaResponse(new LambdaRequest(req));
        return lambdaResponse.toHttpResponse().updateHeaders(this::withCors);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[" + handler + ", cors=" + corsHeaders + "]";
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
}
