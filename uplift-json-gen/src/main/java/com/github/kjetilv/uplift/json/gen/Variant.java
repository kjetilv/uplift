package com.github.kjetilv.uplift.json.gen;

import module java.base;
import module java.compiler;
import com.github.kjetilv.uplift.json.callbacks.MapCallbacks;

import static com.github.kjetilv.uplift.json.gen.GenUtils.*;

enum Variant {

    PRIMITIVE() {
        @Override
        String callbackHandler(TypeElement builderType, RecordComponentElement element, TypeMirror generated) {
            return builderClassPlain(builderType) + "::" + setter(element);
        }
    },

    PRIMITIVE_LIST() {
        @Override
        String callbackHandler(TypeElement builderType, RecordComponentElement element, TypeMirror generated) {
            return builderClassPlain(builderType) + "::" + adder(element);
        }
    },

    GENERATED() {
        @Override
        String callbackHandler(TypeElement builderType, RecordComponentElement element, TypeMirror generated) {
            return "(callbacks, builder) -> " +
                   callbacksClassPlain(generated) +
                   ".create(callbacks, builder::" + setter(
                element) + ")";
        }
    },

    GENERATED_LIST() {
        @Override
        String callbackHandler(TypeElement builderType, RecordComponentElement element, TypeMirror generated) {
            return "(callbacks, builder) -> " +
                   callbacksClassPlain(generated) +
                   ".create(callbacks, builder::" + adder(element) + ")";
        }
    },

    GENERIC_MAP() {
        @Override
        String callbackHandler(TypeElement builderType, RecordComponentElement element, TypeMirror generated) {
            return "(callbacks, builder) -> " + MapCallbacks.class.getName() +
                   ".create(callbacks, builder::" + setter(element) + ")";
        }
    },

    ENUM() {
        @Override
        Optional<String> midTerm(RecordComponentElement element, TypeMirror internalType) {
            return Optional.of(element.asType().toString() + "::valueOf");
        }

        @Override
        String callbackHandler(TypeElement builderType, RecordComponentElement element, TypeMirror generated) {
            return builderClassPlain(builderType) + "::" + setter(element);
        }
    },

    ENUM_LIST {
        @Override
        Optional<String> midTerm(RecordComponentElement element, TypeMirror internalType) {
            return Optional.of(internalType.toString() + "::valueOf");
        }

        @Override
        String callbackHandler(TypeElement builderType, RecordComponentElement element, TypeMirror generated) {
            return builderClassPlain(builderType) + "::" + adder(element);
        }
    };

    Optional<String> midTerm(RecordComponentElement element, TypeMirror internalType) {
        return Optional.empty();
    }

    abstract String callbackHandler(TypeElement builderType, RecordComponentElement element, TypeMirror generated);
}
