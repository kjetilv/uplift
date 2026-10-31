package com.github.kjetilv.uplift.json.gen;

import module java.base;
import module java.compiler;

/// A problem with user code, to be reported at the given element
final class GenException extends RuntimeException {

    private final transient Element element;

    GenException(Element element, String message) {
        super(message);
        this.element = Objects.requireNonNull(element, "element");
    }

    Element element() {
        return element;
    }
}
