package com.github.kjetilv.uplift.flambda;

import module java.base;
import com.github.kjetilv.uplift.hash.Hash;
import com.github.kjetilv.uplift.hash.HashKind;
import com.github.kjetilv.uplift.lambda.RequestOut;

public record LambdaReq(
    Hash<HashKind.K128> id,
    RequestOut out
) {

    public LambdaReq(RequestOut out) {
        this(HashKind.K128.random(), out);
    }

    public LambdaReq {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(out, "out");
    }
}
