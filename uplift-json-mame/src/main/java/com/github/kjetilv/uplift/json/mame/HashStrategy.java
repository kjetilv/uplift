package com.github.kjetilv.uplift.json.mame;

import module uplift.edamame;
import module uplift.hash;

import java.util.function.Supplier;

public interface HashStrategy<H extends HashKind<H>> {

    Hash<H> hashLeaf(Object object);

    Supplier<HashBuilder<byte[], H>> supplier();

    boolean preserveNulls();

    HashedTree<String, H> getNull();
}
