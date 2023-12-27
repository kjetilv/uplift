package com.github.kjetilv.uplift.json.annpro;

import javax.lang.model.element.RecordComponentElement;
import javax.lang.model.element.TypeElement;
import java.util.Optional;

import static com.github.kjetilv.uplift.json.annpro.Gen.*;

record AttributeType(
    String event,
    RecordComponentElement element,
    Variant variant,
    TypeElement internalType
) {

    AttributeType(String event, RecordComponentElement element) {
        this(event, element, Variant.PRIMITIVE, null);
    }

    String handler(TypeElement builderType) {
        return "on" + event + "(\"" +
               element.getSimpleName() + "\", " +
               variant.midTerm(element, internalType)
                   .map(term -> term + ", ").orElse("") +
               variant.handler(builderType, element, internalType) +
               ")";
    }

    enum Variant {
        PRIMITIVE() {
            @Override
            String handler(TypeElement builderType, RecordComponentElement element, TypeElement generated) {
                return builderClass(builderType) + "::" + setter(element);
            }
        },
        PRIMITIVE_LIST() {
            @Override
            String handler(TypeElement builderType, RecordComponentElement element, TypeElement generated) {
                return builderClass(builderType) + "::" + adder(element);
            }
        },
        GENERATED() {
            @Override
            String handler(TypeElement builderType, RecordComponentElement element, TypeElement generated) {
                return "() -> " + callbacksClass(generated) + ".create(this, builder()::" + setter(element) + ")";
            }
        },
        GENERATED_LIST() {
            @Override
            String handler(TypeElement builderType, RecordComponentElement element, TypeElement generated) {
                return "() -> " + callbacksClass(generated) + ".create(this, builder()::" + adder(element) + ")";
            }
        },
        ENUM() {
            @Override
            Optional<String> midTerm(RecordComponentElement element, TypeElement internalType) {
                return Optional.of(element.asType().toString() + "::valueOf");
            }

            @Override
            String handler(TypeElement builderType, RecordComponentElement element, TypeElement generated) {
                return builderClass(builderType) + "::" + setter(element);
            }
        },
        ENUM_LIST {
            @Override
            Optional<String> midTerm(RecordComponentElement element, TypeElement internalType) {
                return Optional.of(internalType.asType().toString() + "::valueOf");
            }

            @Override
            String handler(TypeElement builderType, RecordComponentElement element, TypeElement generated) {
                return builderClass(builderType) + "::" + adder(element);
            }
        };

        @Override
        public String toString() {
            return super.toString();
        }

        Optional<String> midTerm(RecordComponentElement element, TypeElement internalType) {
            return Optional.empty();
        }

        abstract String handler(TypeElement builderType, RecordComponentElement element, TypeElement generated);
    }
}
