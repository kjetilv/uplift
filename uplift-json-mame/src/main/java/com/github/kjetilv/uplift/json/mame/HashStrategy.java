package com.github.kjetilv.uplift.json.mame;

import com.github.kjetilv.uplift.edamame.HashedTree;
import com.github.kjetilv.uplift.hash.Hash;
import com.github.kjetilv.uplift.hash.HashBuilder;
import com.github.kjetilv.uplift.hash.HashKind;

import java.util.function.Supplier;

public interface HashStrategy<H extends HashKind<H>> {

    Hash<H> hashLeaf(Object object);

    Supplier<HashBuilder<byte[], H>> supplier();

    boolean preserveNulls();

    HashedTree<String, H> getNull();
}
