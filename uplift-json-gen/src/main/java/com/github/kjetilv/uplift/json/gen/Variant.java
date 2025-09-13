package com.github.kjetilv.uplift.json.gen;

import com.github.kjetilv.uplift.json.callbacks.MapCallbacks;

import javax.lang.model.element.RecordComponentElement;
import javax.lang.model.element.TypeElement;
import java.util.Optional;

import static com.github.kjetilv.uplift.json.gen.GenUtils.*;

enum Variant {

    PRIMITIVE() {
        @Override
        String callbackHandler(TypeElement builderType, RecordComponentElement element, TypeElement generated) {
            return builderClassPlain(builderType) + "::" + setter(element);
        }
    },

    PRIMITIVE_LIST() {
        @Override
        String callbackHandler(TypeElement builderType, RecordComponentElement element, TypeElement generated) {
            return builderClassPlain(builderType) + "::" + adder(element);
        }
    },

    GENERATED() {
        @Override
        String callbackHandler(TypeElement builderType, RecordComponentElement element, TypeElement generated) {
            return "(callbacks, builder) -> " +
                   callbacksClassPlain(generated) +
                   ".create(callbacks, builder::" + setter(
                element) + ")";
        }
    },

    GENERATED_LIST() {
        @Override
        String callbackHandler(TypeElement builderType, RecordComponentElement element, TypeElement generated) {
            return "(callbacks, builder) -> " +
                   callbacksClassPlain(generated) +
                   ".create(callbacks, builder::" + adder(element) + ")";
        }
    },

    GENERIC_MAP() {
        @Override
        String callbackHandler(TypeElement builderType, RecordComponentElement element, TypeElement generated) {
            return "(callbacks, builder) -> " + MapCallbacks.class.getName() +
                   ".create(callbacks, builder::" + setter(element) + ")";
        }
    },

    ENUM() {
        @Override
        Optional<String> midTerm(RecordComponentElement element, TypeElement internalType) {
            return Optional.of(element.asType().toString() + "::valueOf");
        }

        @Override
        String callbackHandler(TypeElement builderType, RecordComponentElement element, TypeElement generated) {
            return builderClassPlain(builderType) + "::" + setter(element);
        }
    },

    ENUM_LIST {
        @Override
        Optional<String> midTerm(RecordComponentElement element, TypeElement internalType) {
            return Optional.of(internalType.asType().toString() + "::valueOf");
        }

        @Override
        String callbackHandler(TypeElement builderType, RecordComponentElement element, TypeElement generated) {
            return builderClassPlain(builderType) + "::" + adder(element);
        }
    };

    Optional<String> midTerm(RecordComponentElement element, TypeElement internalType) {
        return Optional.empty();
    }

    abstract String callbackHandler(TypeElement builderType, RecordComponentElement element, TypeElement generated);
}
