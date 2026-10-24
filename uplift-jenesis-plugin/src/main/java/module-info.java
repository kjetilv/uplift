import build.jenesis.BuildModuleFactory;
import com.kjetilv.github.uplift.plugins.jenesis.UpliftFactory;
/// @jenesis.bom pin-jenesis.properties
/// @jenesis.release 25
/// @jenesis.bom pin-aws.properties
/// @jenesis.bom pin-jenesis.properties
/// @jenesis.bom pin-jenesis.properties
module uplift.jenesis.plugin {
    requires uplift.plugin.core;
    requires uplift.flogs;
    requires build.jenesis;

    provides BuildModuleFactory with UpliftFactory;
}
