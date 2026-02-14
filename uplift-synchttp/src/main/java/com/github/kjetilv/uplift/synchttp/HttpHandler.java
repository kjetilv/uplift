package com.github.kjetilv.uplift.synchttp;

import com.github.kjetilv.uplift.synchttp.rere.HttpReq;
import com.github.kjetilv.uplift.synchttp.write.HttpResponseCallback;

public interface HttpHandler {

    void handle(HttpReq httpReq, HttpResponseCallback callback);
}
