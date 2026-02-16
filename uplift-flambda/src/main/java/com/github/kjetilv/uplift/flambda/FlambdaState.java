package com.github.kjetilv.uplift.flambda;

import com.github.kjetilv.uplift.hash.Hash;
import com.github.kjetilv.uplift.hash.HashKind.K128;
import com.github.kjetilv.uplift.util.Non;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.function.Consumer;

import static java.util.Objects.requireNonNull;

class FlambdaState {

    private final BlockingQueue<LambdaReq> reqQueue;

    private final Sync<Hash<K128>, LambdaReq> syncRequests = new Sync<>();

    private final Sync<Hash<K128>, LambdaRes> syncResponses = new Sync<>();

    FlambdaState(int queueLength) {
        this.reqQueue = new ArrayBlockingQueue<>(Non.negativeOrZero(queueLength, "queueLength"));
    }

    void exchange(LambdaReq lambdaReq, Consumer<LambdaRes> responseHandler) {
        requireNonNull(lambdaReq, "lambdaReq");
        requireNonNull(responseHandler, "responseHandler");
        LambdaRes polled = null;
        try {
            reqQueue.put(lambdaReq);
            polled = syncResponses.get(lambdaReq.id());
            responseHandler.accept(polled);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Failed to put " + lambdaReq, e);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to put " + lambdaReq + " -> " + polled, e);
        }
    }

    LambdaReq fetchRequest() {
        try {
            var lambdaReq = reqQueue.take();
            syncRequests.put(lambdaReq.id(), lambdaReq);
            return lambdaReq;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(e);
        }
    }

    void submitResponse(LambdaRes lambdaRes) {
        syncResponses.put(
            requireNonNull(lambdaRes, "lambdaRes").id(),
            lambdaRes
        );
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[" +
               "[" + reqQueue.size() + "]" +
               " req:" + syncRequests +
               " res:" + syncResponses +
               "]";
    }
}
