package com.github.kjetilv.uplift.json.mame;

import module java.base;
import com.github.kjetilv.uplift.edamame.HashedTree;
import com.github.kjetilv.uplift.edamame.KeyHandler;
import com.github.kjetilv.uplift.hash.HashBuilder;
import com.github.kjetilv.uplift.hash.HashKind;
import com.github.kjetilv.uplift.json.Callbacks;
import com.github.kjetilv.uplift.util.Bytes;

abstract sealed class StructureClimber<H extends HashKind<H>>
    extends AbstractClimber<H>
    permits ListClimber, MapClimber {

    private final Supplier<HashBuilder<Bytes, H>> newBuilder;

    private final Callbacks parent;

    private final Consumer<HashedTree<String, H>> onDone;

    StructureClimber(
        HashStrategy<H> hashStrategy,
        KeyHandler<String> keyHandler,
        Consumer<HashedTree<String, H>> cacher,
        Consumer<HashedTree<String, H>> onDone,
        Callbacks parent
    ) {
        super(hashStrategy, keyHandler, cacher);
        this.onDone = onDone;
        this.parent = parent;
        this.newBuilder = hashStrategy.supplier();
    }

    @Override
    protected final void done(HashedTree<String, H> tree) {
        cache(tree);
        set(tree);
    }

    /// The structure is hashed when it is complete, from the children it actually holds.
    ///
    /// @return Parent callbacks
    protected Callbacks close() {
        onDone.accept(hashedTree(newBuilder.get()));
        return parent;
    }

    /// @param builder New hash builder
    /// @return Hashed tree for the completed structure
    protected abstract HashedTree<String, H> hashedTree(HashBuilder<Bytes, H> builder);

    protected abstract void set(HashedTree<String, H> tree);
}
