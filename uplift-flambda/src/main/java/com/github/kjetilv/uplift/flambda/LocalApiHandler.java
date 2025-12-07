package com.github.kjetilv.uplift.flambda;

import module java.base;
import com.github.kjetilv.uplift.asynchttp.HttpAsyncChannelHandler;
import com.github.kjetilv.uplift.asynchttp.HttpReq;
import com.github.kjetilv.uplift.asynchttp.HttpRes;
import com.github.kjetilv.uplift.util.CaseInsensitiveHashMap;

record LocalApiHandler(LocalLambdaHandler handler, Map<String, List<String>> corsHeaders)
    implements HttpAsyncChannelHandler.Server {

    LocalApiHandler(LocalLambdaHandler handler, CorsSettings corsHeaders) {
        Objects.requireNonNull(corsHeaders, "cors");
        this(
            Objects.requireNonNull(handler, "handler"),
            CaseInsensitiveHashMap.wrap(Map.of(
                "Access-Control-Allow-Origin", List.of(corsHeaders.originValue()),
                "Access-Control-Allow-Methods", List.of(corsHeaders.methodsValue()),
                "Access-Control-Allow-Headers", List.of(corsHeaders.headersValue()),
                "Access-Control-Max-Age", List.of("86400"),
                "Access-Control-Allow-Credentials", List.of(corsHeaders.credentialsValue())
            ))
        );
    }

    @Override
    public HttpRes handle(HttpReq req) {
        if (req.isCORS()) {
            return new HttpRes(OK, corsHeaders, null, req.id());
        }
        var lambdaResponse = handler.lambdaResponse(new LambdaRequest(req));
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
        return getClass().getSimpleName() + "[" + handler + "]";
    }
}
