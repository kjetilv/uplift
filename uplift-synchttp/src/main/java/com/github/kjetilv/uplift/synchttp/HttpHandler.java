package com.github.kjetilv.uplift.synchttp;

import com.github.kjetilv.uplift.synchttp.rere.HttpReq;
import com.github.kjetilv.uplift.synchttp.write.HttpResponseCallback;

public interface HttpHandler {

    /// Handles one request. Called on the connection's own thread.
    ///
    /// The request is only valid until this method returns, see [HttpReq]. The response must be written
    /// before this method returns. A response that has a status but is not complete, and has no declared
    /// content length, is sent with no body. A response without a status gives 500.
    ///
    /// @param httpReq  Request
    /// @param callback Response
    void handle(HttpReq httpReq, HttpResponseCallback callback);
}
