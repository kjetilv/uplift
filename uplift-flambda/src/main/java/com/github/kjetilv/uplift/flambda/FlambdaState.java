package com.github.kjetilv.uplift.flambda;

import com.github.kjetilv.uplift.hash.Hash;
import com.github.kjetilv.uplift.hash.HashKind.K128;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.function.Consumer;

class FlambdaState {

    private final BlockingQueue<LambdaReq> reqQueue;

    private final SyncPoint<Hash<K128>, LambdaReq> requests = new SyncPoint<>();

    private final SyncPoint<Hash<K128>, LambdaRes> responses = new SyncPoint<>();

    FlambdaState(int queueLength) {
        this.reqQueue = new ArrayBlockingQueue<>(queueLength);
    }

    void exchange(LambdaReq lambdaReq, Consumer<LambdaRes> responseHandler) {
        try {
            reqQueue.put(lambdaReq);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Failed to put " + lambdaReq, e);
        }
        LambdaRes polled = responses.get(lambdaReq.id());
        responseHandler.accept(polled);
    }

    LambdaReq fetchRequest() {
        try {
            var lambdaReq = reqQueue.take();
            requests.put(lambdaReq.id(), lambdaReq);
            return lambdaReq;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(e);
        }
    }

    void submitResponse(LambdaRes lambdaRes) {
        responses.put(lambdaRes.id(), lambdaRes);
    }

    record Exchange(Hash<K128> id, LambdaReq req, LambdaRes res) {

    }
}
