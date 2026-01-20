package com.github.kjetilv.uplift.flambda;

import module java.base;
import com.github.kjetilv.uplift.hash.Hash;
import com.github.kjetilv.uplift.hash.HashKind.K128;
import com.github.kjetilv.uplift.lambda.ResponseIn;

record LambdaRes(
    Hash<K128> id,
    ResponseIn in
) {
}
