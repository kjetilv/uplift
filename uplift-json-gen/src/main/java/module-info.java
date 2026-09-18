module uplift.json.gen {
    requires java.compiler;
    requires uplift.hash;
    requires uplift.json;
    requires static uplift.json.anno;

    exports com.github.kjetilv.uplift.json.gen;
    exports com.github.kjetilv.uplift.json.gen.trie;
}
