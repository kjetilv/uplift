package com.github.kjetilv.uplift.json.gen;

import module java.base;
import module java.compiler;

/// Names derived from the element structure. Never from `toString()`.
final class Names {

    /// @param typeElement Type
    /// @return Name relative to the package, such as `Eagle.Nest`
    static String relativeName(TypeElement typeElement) {
        return String.join(".", nesting(typeElement));
    }

    /// @param typeElement Type
    /// @return Name relative to the package, with nesting flattened, such as `Eagle_Nest`
    static String flatName(TypeElement typeElement) {
        return String.join("_", nesting(typeElement));
    }

    /// @param packageElement Package
    /// @param name Name relative to the package
    /// @return Qualified name
    static String qualified(PackageElement packageElement, String name) {
        return packageElement.isUnnamed()
            ? name
            : packageElement.getQualifiedName() + "." + name;
    }

    static String upcased(String string) {
        return Character.toUpperCase(string.charAt(0)) + string.substring(1);
    }

    /// @param string String
    /// @return A Java string literal
    static String quoted(String string) {
        return "\"" + string.replace("\\", "\\\\").replace("\"", "\\\"") + "\"";
    }

    private Names() {
    }

    private static List<String> nesting(TypeElement typeElement) {
        return Stream.iterate(
                (Element) typeElement,
                TypeElement.class::isInstance,
                Element::getEnclosingElement
            )
            .map(element ->
                element.getSimpleName().toString())
            .toList()
            .reversed();
    }
}
