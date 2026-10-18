import build.jenesis.BuildModuleFactory;

module uplift.jenesis.plugin {
    requires uplift.plugin.core;
    requires build.jenesis;

    provides BuildModuleFactory with com.kjetilv.github.uplift.plugins.jenesis.UpliftFactory;
}
