import build.jenesis.BuildModuleFactory;
import com.kjetilv.github.uplift.plugins.jenesis.UpliftFactory;

/// @jenesis.bom pin-jenesis.properties
/// @jenesis.release 25
module uplift.jenesis.plugin {
    requires uplift.plugin.core;
    requires build.jenesis;

    provides BuildModuleFactory with UpliftFactory;
}
